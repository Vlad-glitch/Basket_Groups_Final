const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

// Export function to trigger on updates to 'places' documents in Firestore
exports.notifyUsers = functions.firestore
    .document("places/{placeId}")
    .onUpdate(async (change, context) => {
      const newValue = change.after.data();
      const previousValue = change.before.data();

      const playersChanged =
            JSON.stringify(newValue.players) !==
            JSON.stringify(previousValue.players);
      const scheduledPlayersChanged =
            JSON.stringify(newValue.scheduledPlayers) !==
            JSON.stringify(previousValue.scheduledPlayers);

      // If either the players or scheduledPlayers field has changed,
      // send notifications to the topic.
      if (playersChanged || scheduledPlayersChanged) {
        // Define the message
        const message = {
          notification: {
            title: "Game Update!",
            body: `The players or scheduled players list at
            ${newValue.title} has changed.`,
          },
          topic: `place_${context.params.placeId}`,
        };

        // Send the message
        await admin.messaging().send(message);
      }
    });
