# Image de développement : compile et lance le backend avec rechargement à chaud.
# Le code source est monté en volume par docker-compose (voir ../docker-compose.yml),
# ce qui permet de modifier le code sans reconstruire l'image.
FROM maven:3.9-eclipse-temurin-21

WORKDIR /app

# Copie du pom.xml en premier pour profiter du cache Docker sur les dépendances
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Le dossier src sera écrasé par le volume monté dans docker-compose,
# cette copie sert uniquement à la construction initiale de l'image.
COPY src ./src

EXPOSE 8080

# spring-boot:run + devtools => rechargement automatique à chaque modification
CMD ["mvn", "spring-boot:run"]
