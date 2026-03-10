# Vitae Backend Deployment Guide

This folder contains the complete Cloudflare Worker API to turn your app's Community feature fully online. Note: Due to prompt hang issues during generation, you will need to run the deployment commands manually from this folder.

## Prerequisites
1. Open your terminal and run `node -v` and `npm -v` to ensure your newly installed Node.js is recognized in your path.
2. Ensure you have a Cloudflare account.

## Step 1: Install Dependencies
Open your terminal inside this `vitae-backend` folder and run:
```bash
npm install -g wrangler
```

## Step 2: Login to Cloudflare
Authenticate the Wrangler CLI with your Cloudflare account. A browser window will open.
```bash
wrangler login
```

## Step 3: Create the Database
We need to create the D1 SQL database where the Templates and Reviews will live.
```bash
wrangler d1 create vitae-db
```
**CRITICAL**: That command will output a `database_id` string (e.g., `5a07xxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`). Open the `wrangler.jsonc` file in this folder and paste that ID into the `database_id` field near the bottom.

## Step 4: Initialize the Database Schema
Now build the actual Tables inside that new database.
```bash
wrangler d1 execute vitae-db --file=./schema.sql
```

## Step 5: Deploy the API
Finally, push the backend API code to the internet.
```bash
wrangler deploy
```

Once deployed, the terminal will give you a live URL (e.g., `https://vitae-backend.yourname.workers.dev`). You can then take that URL and start integrating it into `MainActivity.java`'s networking client!
