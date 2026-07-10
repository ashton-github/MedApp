# Image de développement : serveur Vite avec rechargement à chaud.
# Le code source est monté en volume par docker-compose (voir ../docker-compose.yml).
FROM node:20-alpine

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

EXPOSE 5173

CMD ["npm", "run", "dev"]
