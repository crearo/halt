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

### TODO 

- Store unlock times in Room

All my use-cases hint at simply storing unlock times in a database and querying them. What might be
a little tricky is handling UTC and local time. Need to find a good way to handle that so that
the code is readable but also works.
This is actually the most important bit. Once I can query such information easily, lotsa power.
Every unlock has to have a corresponding lock. The gap between these two is the phone usage time 
for that period. Fun, fun. This has edge cases for which I'll have to write good test cases. 

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
    