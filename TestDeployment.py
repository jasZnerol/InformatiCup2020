import subprocess
from multiprocessing.dummy import Pool as ThreadPool

wins = 0
loss = 0

def playGame(seed):
    print("Started %d" % (seed))
    result = subprocess.run(['./ic20_linux', '-t', '0', '-s', str(seed), '-u', 'https://udi8pt9vo9.execute-api.us-east-1.amazonaws.com/Beta/'], stdout=subprocess.PIPE).stdout.decode('utf-8')
    if("win" in result):
        wins += 1
        print("Seed: %d Outcome: Win" % (seed))
        print("Winrate %d" % (wins / (wins + loss)))
        return (seed, "win")
    elif("loss" in result):
        loss += 1
        print("Seed: %d Outcome: Win" % (seed))
        print("Winrate %d" % (wins / (wins + loss)))
        return (seed, "loss")

# Make the Pool of workers
pool = ThreadPool(10)

# Open the URLs in their own threads
# and return the results
results = pool.map(playGame, list(range(1, 12)))

print(results)

# Close the pool and wait for the work to finish
pool.close()
pool.join()