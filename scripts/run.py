import sys
import importlib
from inspect import signature
import json

argv = sys.argv
fnName = argv[1]
path = "usr."+fnName
mod = importlib.import_module(path)
try:
    func = getattr(mod, fnName)
except AttributeError:
    print("no such function")
    exit(-1)

numargs = len(signature(func).parameters)
if (len(argv) - 2 < numargs):
    print("not enough arguments")
    exit(-1)
args = [json.loads(argv[i+2]) for i in range(numargs)]

print(json.dumps(func(*args)))
