# --- Stage 1: Build the Application ---
FROM node:20-alpine AS build
WORKDIR /app

# Copy package.json and package-lock.json from the Frontend folder
# This is key for efficient Docker caching
COPY vite-project/package*.json ./

# Install dependencies
# We use --prefix to ensure node_modules are installed inside the current /app directory
RUN npm install

# Copy the rest of the application code from the Frontend folder
COPY vite-project/ .

# Build the application for production
RUN npm run build

# --- Stage 2: Serve the Application with NGINX ---
FROM nginx:alpine AS production

# Copy the custom NGINX configuration
# Assuming nginx.conf is next to the Dockerfile
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy the built application files from the 'build' stage
# The build output is in /app/dist
COPY --from=build /app/dist /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# NGINX starts automatically