// api.js had 0% test coverage in our last V8 report, despite handling
// JWT auth, token refresh on 401, and the concurrent-request queue —
// critical logic worth testing. Adding coverage here toward our
// 70-80% target from the cahier des charges.