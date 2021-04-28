import * as functions from 'firebase-functions';

const admin = require('firebase-admin');
admin.initializeApp();

const region = 'europe-west3';
const db = admin.firestore();
db.settings({ignoreUndefinedProperties: true});

exports.uploadModelStats = functions.region(region).https.onCall((data, request) => {
    const collectionRef = db.collection('modelStats');
    const dataToSave = {
        id: data.id,
        timestamp: admin.firestore.Timestamp.fromMillis(data.timestamp),
        modelType: data.modelType,
        validationMetrics: {
            sensitivity: data.sensitivity,
            specificity: data.specificity,
            precision: data.precision,
            falseDiscoveryRate: data.falseDiscoveryRate,
            falsePositiveRate: data.falsePositiveRate,
            f1score: data.f1score,
        },
        confusionMatrix: data.confusionMatrix,
        oobMetrics: {
            fitTime: data.oobFitTime,
            scoreTime: data.oobScoreTime,
            error: data.oobError,
            accuracy: data.oobAccuracy
        },
        trainingInfo: {
            testSize: data.testSize,
            trainSize: data.trainSize,
            anxiousCountTrain: data.anxiousCountTrain,
            anxiousCountTest: data.anxiousCountTest
        }
    };

    functions.logger.log(dataToSave);

    const doc = collectionRef.doc()

    return doc.set(dataToSave)
        .then(() => {
            return "ok";
        });
    
});

