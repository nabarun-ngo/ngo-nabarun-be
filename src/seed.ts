import "dotenv/config";
import { PrismaPg } from "@prisma/adapter-pg";
import { PrismaClient } from "#prisma";
import { seedAuth } from "./seeds/auth/auth.seeder";
import { seedJsonStore } from "./seeds/json-store/json-store.seeder";

const prisma = new PrismaClient({
    adapter: new PrismaPg({
        connectionString: process.env.DATABASE_URL
    })
});

async function main() {
    // Auth seeding
    await seedAuth(prisma);
    // JsonStore seeding
    await seedJsonStore(prisma);
}

main()
    .catch((error) => {
        console.error(error);
        process.exitCode = 1;
    })
    .finally(() => prisma.$disconnect());