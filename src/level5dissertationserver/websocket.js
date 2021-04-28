'use strict';

const Uws = require('uWebSockets.js');
const admin = require('firebase-admin');

const getRandomItem = iterable => {
    const keys = [...iterable.keys()];
    const rKey = keys[Math.floor(Math.random() * iterable.size)];
    const value = iterable.get(rKey);
    return [rKey, value];
};

const serviceAccount = require('./serviceAccount.json');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const collectionRef = db.collection('connections');

async function addConnectionToFirestore(wsID, clientID) {
    const docRef = collectionRef.doc();
    const timestamp = Date.now();
    await docRef.set({
        timestamp: timestamp,
        caller: wsID,
        receiver: clientID
    });
}

try {
    let connectedClients = new Map(); // for those that are listening for anyone to get some data
    let callerToClient = new Map() // maps caller ws to client [socket, ID]
    let clientToCaller = new Map(); // reverse mapping for the above
    
    Uws.App({})
        .ws('/socket', {
            idleTimeout: 300,
            message: (ws, message, isBinary) => {
                const jsonM = JSON.parse(new TextDecoder('utf-8').decode(message));
                console.log(connectedClients);
                if (jsonM.hasOwnProperty('type') && jsonM.hasOwnProperty('id')) {
                    let type = jsonM.type;
                    const wsID = jsonM.id;
                    if (type === 'REQUEST_TO_CONNECT') {
                        if (connectedClients.size >= 1) {
                            //    init the offer
                            const [clientID, clientWS] = getRandomItem(connectedClients);
                            
                            if (clientID !== wsID) {
                                callerToClient.set(ws, clientWS);
                                clientToCaller.set(clientWS, ws);

                                if (connectedClients.has(wsID))
                                    connectedClients.delete(wsID);

                                let toRemove;
                                for (const [id, socket] in connectedClients.entries()) {
                                    if (socket === ws) {
                                        toRemove = id;
                                        break;
                                    }
                                }
                                connectedClients.delete(toRemove);

                                addConnectionToFirestore(wsID, clientID).finally(() => console.log('sent to firestore'));
                                
                                console.log('SEND_OFFER');
                                ws.send(JSON.stringify({response: 'SEND_OFFER'}), false); // tell the caller to send the offer
                            } else {
                                console.log('device is attempting to connect with itself');
                                ws.send(JSON.stringify({response: 'CONNECTED_TO_WS'}), false);
                            }
                        } else {
                            //    no other clients are connected at this point. tell the device to wait later
                            console.log('CONNECTED_TO_WS');
                            if (!connectedClients.has(wsID)) {
                                connectedClients.set(wsID, ws);
                            }
                            ws.send(JSON.stringify({response: 'CONNECTED_TO_WS'}), false);
                        }
                    }
                } else {
                    // should be all webrtc related stuff
                    // at this point we assume that the client and caller are mapped, so just forward to each other
                    // if the websocket is a key in the callerToClient, then this is a message from the original caller
                    let socket;
                    if (callerToClient.has(ws)) {
                        console.log('sending to client');
                        socket = callerToClient.get(ws);
                    } else if (clientToCaller.has(ws)) {
                        console.log('sending to caller');
                        socket = clientToCaller.get(ws);
                    } else {
                        console.log('websocket does not exist');
                    }
                    if (socket !== undefined) {
                        socket.send(message, isBinary);
                    }
                }

            },
            close: (ws, code, message) => {
                try {
                    const jsonM = JSON.parse(new TextDecoder('utf-8').decode(message));
                    const reason = jsonM.hasOwnProperty('closeRequestReason') ? jsonM.closeRequestReason : 'Reason not given';
                    const userID = jsonM.hasOwnProperty('id') ? jsonM.id : undefined;

                    console.log(`code: ${code}, reason: ${reason}`);
                    
                    if (userID !== undefined && connectedClients.has(userID)) {
                        let res = connectedClients.delete(userID);
                        console.log(`deleted from connectedClients: ${res}`);
                    }
                } catch (e) {
                    console.log(e);
                    let toRemove;
                    connectedClients.forEach((value, key) => {
                        if (value === ws) {
                            toRemove = key;
                        }
                    });
                    if (toRemove !== undefined) {
                        let res = connectedClients.delete(toRemove);
                        console.log(`deleted from connectedClients: ${res}`);
                    }
                    
                }

                console.log(`websocket closing`);
                if (callerToClient.has(ws)) {
                    let res = callerToClient.delete(ws);
                    console.log(`deleted from callerToClient: ${res}`);
                } else if (clientToCaller.has(ws)) {
                    let res = clientToCaller.delete(ws);
                    console.log(`deleted from clientToCaller: ${res}`);
                }
            },
            open: ws => {
                console.log('websocket opened');
            },
            drain: ws => {
                console.log('draining')
            }
        })
        .listen('0.0.0.0', Number.parseInt(process.env.PORT) || 9001, (token => {
            if (token) {
                console.log(`listening to port ${process.env.PORT || 9001}`);
            } else {
                console.log('failed to listen')
            }
        }));
} catch (e) {
    console.log(e);
    process.exit(1);
}
