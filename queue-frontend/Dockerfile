# Etapa 1 — Build do Angular
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration production


# Etapa 2 — Nginx
FROM nginx:alpine
# Copia o build do Angular
COPY --from=build /app/dist/queue-frontend/browser /usr/share/nginx/html
# Copia o template do Nginx
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
