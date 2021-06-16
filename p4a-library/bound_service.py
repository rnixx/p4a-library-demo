print ('###################### start bound service #######################')


import time
import asyncio
import websockets


counter = 0
async def response(websocket, path):
    global counter
    counter += 1
    msg = await websocket.recv()
    print("msg:", msg, counter)
    await websocket.send("remote echo[%s]: %s" % (counter, msg))


def start_server():
    server = websockets.serve(response, '127.0.0.1', 8082)
    asyncio.get_event_loop().run_until_complete(server)
    asyncio.get_event_loop().run_forever()


start_server()


print ('bound service ended')
