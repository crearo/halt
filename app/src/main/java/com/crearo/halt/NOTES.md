Just noting things down over here because it's the easiest place to track thought. Don't wanna be
making yet another Google Doc and then link shit. Plus markdown is awesome.

### Notes

- Remember, I'm making this app for me, based on my habits. I can consider what features and
generalizations it should have when I have proven it works for me.
- You can lead a horse to water, but you can't make it drink. If you force its neck down, it'll kick
 your neck.
 This _terrific_ analogy suggests that users will disable an app that forces them to do something. 
 Ex: I keep unblocking myself from AppBlock, or from Digital Well-being, and since it doesn't have 
 an auto restart, I never end up using it on a regular basis. It's on for a while, and then one slip,
 and the limit is forever at 4 hours a day and I don't go and manually change it back again.  
 *tldr:*, this app can help your will power, but it's up to you to not use your phone. It won't 
 stop you from using any of your apps entirely ever. It'll just make you mindful of what you're 
 doing.
- My aim is to primarily block Instagram and mindless scrolling, or mindless watching of videos. 
So, I'm going to heavily distinguish between these. If it is possible to know which website is 
currently being used (it is because AppBlock has it without root), then I can track that too. 

### TODO 

#### Store unlock times in Room

All my use-cases hint at simply storing unlock times in a database and querying them. What might be
a little tricky is handling UTC and local time. Need to find a good way to handle that so that
the code is readable but also works.
This is actually the most important bit. Once I can query such information easily, lotsa power.
Every unlock has to have a corresponding lock. The gap between these two is the phone usage time 
for that period. Fun, fun. This has edge cases for which I'll have to write good test cases. 

UnlockStat: id, unlock_time, lock_time, duration

Also this table is going to become huge over time, and I'll need to find a way to limit how big it 
gets. Maybe compress results from a month ago. 

#### Cases

- Identifying sleep / wake up times

    - When I slept: Post 10pm, the last time I unlocked my phone is when I slept.
    - When I woke up: Post 5am, the first time I unlock my phone is when I woke up.
    
- Identifying excess usage in one sitting

    A thread running continuously which will check if the phone is currently unlocked, and the time
    since it was last unlocked. The time diff is all I need to fire an event for excess usage.
    Nice, this could be an awesome Rx app. Agh, why does my mind only think of Rx. I should update 
    my know-how of current day Android. When all you have is a hammer, everything looks like a nail.

- Identifying excess usage in the last 30 minutes

    Another use case is multiple unlocks and lots of fragmented usages over a period of time. Easy,
    just query the usage times in the last 30 minutes. Note, every unlock has to have a corresponding 
    lock.

- Identifying unlocking the phone too often

    This is easy innit. Just query the number of times the phone was unlocked in the last x minutes.
    
#### Store app usage in Room

The usage stats library is limited in that it only gives me usage throughout the day. What I want
is the power to query this at any point. Say I would like to know how much an app has been used 
in the last 90 minutes and block on the basis of that.

- Blocking App Opening

    I will not block an app from being opened. But I'll try to make it hard to open.
    Probably re-use the thread as above which continuously polls for the current app, and restrict on
    the basis of it.
    
#### The UI

This is what has kept my from sleeping so many nights. I keep coming back to this project, try to make
bubbles and what not, and never do the hard work of actually identifying what I am trying to achieve, 
and the algo with which this will work. I have that now. And frankly, all I want is the app to look
good-ish at the moment. It has to be functional. Fancy floating characters and personalisation that 
bring the phone to life will come next. Or maybe never. Maybe that's meant for over-users who enjoy
overusing their phone.

I can only think of 2 things that will show up.

- Overuse Reminder
    Have something from underneath show up? This should just be a flash or something that happens 
    for a short period.
- Overuse Alert
    An idea: make it rain on the phone screen. This makes the phone / the app harder to use, but not
    entirely block you from being able to use it.
- Intent based opening of the phone
    This lets you choose what you're gonna do for this period that you unlock your phone. 
    Maybe have a list of apps you specifically use during this time usually? 
    Or choose to open the phone for Social Media / no social media. That's it. As easy as that. 
    If you're in for non-social media, block all notifications too so that you aren't distracted.      
    
#### Battery Drain because of this app

Yeah, I actually want to know the affect of an app that runs in the background that does seemingly 
not much than polling. Would be an interesting thing to see.


#### Java Time

Fuck why is dealing with time so hard. And why aren't there any tutorials on it? I'm stuck at how 
to store date and time in the database. So there are two views of time: machine view and the human
view. Machine view is time in a linear monotonically increasing number line since epoch. 
Human view is hour, day, month, with timezone. 
So machine view is more of a single source of truth. 
Human view is what you should convert the single source of truth based on what your application needs
to do for humans. Just my thoughts, I'm really clueless.
From the little sense I can make of it, I think I should be storing date and time from epoch without
the timezone. Take for example, what if I unlock my phone in Germany before I get on a plane to USA,
and then lock it when I land where time updates. It'd fuck up all logic that I've built into the db.
So then I could potentially store timezone along with it, but that's stupid, extra work, and a real
edge case. 
So, the machine view. Time since epoch is best.