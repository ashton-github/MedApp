// Exécuté automatiquement au premier démarrage du conteneur mongodb
// (dossier docker-entrypoint-initdb.d)

db = db.getSiblingDB('medapp');

// Collection utilisateurs (authentification)
db.createCollection('users');
db.users.createIndex({ email: 1 }, { unique: true });

// Collection patients
db.createCollection('patients');
db.patients.createIndex({ nom: 1, prenom: 1 });

// Collection ordonnances
db.createCollection('ordonnances');
db.ordonnances.createIndex({ patientId: 1 });
db.ordonnances.createIndex({ medecinId: 1 });

print('Collections et index initiaux créés pour la base "medapp".');
