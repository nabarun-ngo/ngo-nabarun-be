const usersToInsert = [
    {
        email: "cashier@nabarun.com",
        firstName: "Cashier",
        lastName: "TestUser"
    },
    {
        email: "admin@nabarun.com",
        firstName: "Admin",
        lastName: "TestUser"
    },
    {
        email: "user@nabarun.com",
        firstName: "User",
        lastName: "TestUser"
    }
];

usersToInsert.forEach(user => {
    const existingUser = db.user_profiles.findOne({ email: user.email });
    if (existingUser == null) {
        db.user_profiles.insertOne({
            email: user.email,
            firstName: user.firstName,
            lastName: user.lastName,
            createdOn: new Date(),
            activeContributor: true,
            publicProfile: true,
            status: "ACTIVE",
            deleted: false,
            loginMethods: "PASSWORD"
        });
        print(`User created with email '${user.email}'.`);
    } else {
        print(`User with email '${user.email}' already exists.`);
    }
});


const cashierAccount = db.accounts.findOne({ _id: 'NACC202410724287A01TEST' });
if(!cashierAccount) {
    const cashierUser = db.user_profiles.findOne({ email: 'cashier@nabarun.com' });
    const treasurerUser = db.user_profiles.findOne({ email: 'treasurer@nabarun.com' });
    if (!cashierUser) {
        print("Cashier user not found. Please ensure the user exists before creating the account.");
    }
    db.accounts.insert({
        _id: 'NACC202410724287A01TEST',
        currentBalance: 0.0,
        openingBalance: 0.0,
        profile: cashierUser._id,
        accountStatus: 'ACTIVE',
        accountType: 'DONATION',
        accountName: cashierUser.firstName + ' ' + cashierUser.lastName,
        activatedOn: new Date(),
        createdOn: new Date(),
        createdById: treasurerUser._id,
        createdByName: treasurerUser.firstName + ' ' + treasurerUser.lastName,
        createdByEmail: treasurerUser.email,
        deleted: false,
        _class: 'ngo.nabarun.app.infra.core.entity.AccountEntity'
    });
    print(`Account created with ID 'NACC202410724287A01TEST'.`);
}

const testEvent = db.events.findOne({title: "Test Event"});
if (!testEvent) {
    db.events.insert({
        title: "Test Event",
        description: "Test Event",
        eventDate: new Date("2025-03-16T06:41:57.759Z"),
        eventLocation: "Test Location",
        eventState: "INTERNAL",
        createdOn: new Date(),
        draft: false,
        eventBudget: 0,
        deleted: false
    });
    print("Test Event inserted successfully.");
}

