import subprocess
from multiprocessing.dummy import Pool as ThreadPool
import codecs

seeds = []
with codecs.open('seeds.txt', mode='r', encoding='utf_8') as f:
    for line in f.read().split("\n"):
        if(line != ""):
            seeds.append(int(line))


wins = 0
loss = 0

def playGame(seed):
    global wins
    global loss
    result = subprocess.run(['./ic20_linux', '-t', '0', '-s', str(seed), '-u', 'https://udi8pt9vo9.execute-api.us-east-1.amazonaws.com/Beta/'], stdout=subprocess.PIPE).stdout.decode('utf-8')
    if("win" in result):
        wins += 1   
        print("Game: %d    Seed: %d    Outcome: Win" % ((wins + loss), seed))
        print("Winrate %f%%" % (wins * 100 / (wins + loss)))
        return (seed, "win")
    elif("loss" in result):
        loss += 1 
        print("Game: %d    Seed: %d    Outcome: Loss" % ((wins + loss), seed))
        print("Winrate %f%%" % (wins * 100 / (wins + loss)))
        return (seed, "loss")

# Make the Pool of workers
pool = ThreadPool(10)

# Open the URLs in their own threads
# and return the results
results = pool.map(playGame, seeds)

print(results)

# Close the pool and wait for the work to finish
pool.close()
pool.join()


