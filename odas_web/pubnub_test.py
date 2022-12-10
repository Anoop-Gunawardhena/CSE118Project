from pubnub.pnconfiguration import PNConfiguration
from pubnub.pubnub import PubNub
from pubnub.callbacks import SubscribeCallback
from pubnub.enums import PNStatusCategory, PNOperationType
import subprocess
import time
import os
from subprocess import check_output
from signal import SIGINT
import socket
#from parser import*

pnconfig = PNConfiguration()

pnconfig.subscribe_key = 'sub-c-abee09f7-35bc-4433-b4ba-ba12da2c5028'
pnconfig.publish_key = 'pub-c-f30c3204-e2ee-4907-9d2a-57f68b84a3e9'
pnconfig.user_id = "Channel_Barcelona"
pubnub = PubNub(pnconfig)

def my_publish_callback(envelope, status):
    # Check whether request successfully completed or not
    if not status.is_error():
        pass  # Message successfully published to specified channel.
    else:
        print("error")
        pass  # Handle message publish error. Check 'category' property to find out possible issue
        # because of which request did fail.
        # Request can be resent using: [status retry];

class MySubscribeCallback(SubscribeCallback):
    def presence(self, pubnub, presence):
        pass  # handle incoming presence data

    def status(self, pubnub, status):
        if status.category == PNStatusCategory.PNUnexpectedDisconnectCategory:
            pass  # This event happens when radio / connectivity is lost

        elif status.category == PNStatusCategory.PNConnectedCategory:
            # Connect event. You can do stuff like publish, and know you'll get it.
            # Or just use the connected event to confirm you are subscribed for
            # UI / internal notifications, etc
            print("connected")
                    
        elif status.category == PNStatusCategory.PNReconnectedCategory:
            pass
            # Happens as part of our regular operation. This event happens when
            # radio / connectivity is lost, then regained.
        elif status.category == PNStatusCategory.PNDecryptionErrorCategory:
            pass
            # Handle message decryption error. Probably client configured to
            # encrypt messages and on live data feed it received plain text.

    def message(self, pubnub, message):
        # Handle new message stored in message.message
        if message.message == "safe to cross":
            pass
            #print("done !!")
        if message.message == "Hello world!":
            pass
        if (message.message == 'record' or message.message[0]  == "record") or message.message == "wait":
                proc = subprocess.Popen(["./record.sh"], stdout =subprocess.PIPE)
                print("priniting now")
                ctr = 0
                while ctr < 2:
                    output = proc.stdout.readline()
                    output = output.decode()
                    output = output[0:len(output)-1]
                    if "closed" in output:
                        ctr = ctr+1
                    print("lolol" + str(output))
                pubnub.publish().channel('Channel-Barcelona').message('kill').pn_async(my_publish_callback)
               

            #os.kill(proc.pid, SIGINT)
            #os.command("killall -9 helectron")
            
        if message.message == "kill":
            #print("killed")
            os.system("killall -9 electron")
            state = check_output("./parse.sh")
            state = state.decode()
            print(len(state))
            state = state[0:len(state)-1].strip()
            print(len(state))
            proc = subprocess.Popen("./clean.sh")
            print(state + " here")
            print(type(state))
            print(type("safe to cross"))
            if state == "safe to cross":
                #print("sent safety")
                pubnub.publish().channel('Channel-Barcelona').message("safe to cross").pn_async(my_publish_callback)
                print("sent safety")
            elif state == "danger":
                pubnub.publish().channel('Channel-Barcelona').message("wait").pn_async(my_publish_callback)
                print("sent danger")
                
            else:
                pubnub.publish().channel('Channel-Barcelona').message('yes').pn_async(my_publish_callback)


            
            

        
        #print(type(message.message))
        #print(type(message))

pubnub.add_listener(MySubscribeCallback())
pubnub.subscribe().channels('Channel-Barcelona').execute()
