import type { User } from "./columns";

export async function load() {
    const users: User[] = [
        {
            name: "Alex Morgan",
            email: "alex.morgan@example.com",
            website: "https://alexmorgan.dev",
            country: "USA"
        },
        {
            name: "Priya Shah",
            email: "priya.shah@example.com",
            website: "https://priyashah.io",
            country: "India"
        },
        {
            name: "Leo Ferreira",
            email: "leo.ferreira@example.com",
            website: "https://leoferreira.com",
            country: "Brazil"
        },
        {
            name: "Maja Novak",
            email: "maja.novak@example.com",
            website: "https://majanovak.eu",
            country: "Croatia"
        }
    ];
    return {
        users,
    };
}
