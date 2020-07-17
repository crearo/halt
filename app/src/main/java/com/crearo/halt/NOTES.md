Just noting things down over here because it's the easiest place to track thought. Don't wanna be
making yet another Google Doc and then link shit. Plus markdown is awesome.

# Notes

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

--

Okay now I'm at a point where all proof-of-tech is working and the backbone of the app is ready.
But, now I'm confused on what I actually want to do.
There are 2 main things:
1. Intent based phone usage. So you are asked what you want to do when you unlock your phone.
    Caveat: not every time you unlock your phone. Only when you've overused it. (TODO DEFINE OVERUSE)
    Two modes to chose from: 1. Focused 2. Social
    Focused: blocks you from opening distracting apps + ensures phone is on DND with notifs hidden.
    Social: Every unlocked.

    Now, choosing between Focused and Social will be different every time you open the phone, so
    that I don't develop muscle memory.
    I will also have to limit how many times a user is allowed to click Social without making the
    apps unusable altogether. How do I do this?
2. Automatic DND. This refers to the notifications only.
    I can't decide how this should work.
    Options:
        - automatic DND after 10pm
        - set DND after 5am and disable it in the morning an hour after you wake up.
        - keep setting DND at random points in the day, especially on over-use of your phone.

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

#### Problem with detecting when the phone is locked/unlocked

The broadcast receiver won't cut it for me. The callback for SCREEN_OFF comes a bit after USER_PRESENT
if you lock / unlock quick enough. What that means is it'll fuck up my logic for tracking un/locks.
That's a tad bit upsetting because then I have to rely on constant polling. That works though.

I'm gonna solve it for the PIN lock use case right now, but will have to try code it out for other
types of screen locks.

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


#### CircleCI

I've never set it up myself but those green badges do make the app look pretty fucking cool. I'll
try and set it up once I have some test cases.


#### Think some more

- I have to set DND off at ~10pm and re-enable it after a few hours after the first use of the
 phone in the morning. This shit is time based and condition based. So should I create a TimePoller,
 or should this happen in DndPoller? I guess DndPoller only. Easiest there.
 No.
 That's wrong. Because that's giving it a whole lot of responsibility.
 Here's when my app is going to enable DND on it's own:
    - Enable DND at ~10pm
    - Disable it some hours after the morning
    - Enable and disable randomly throughout the day based on usage (the exact way I will decide
      when to do it still has to be decided)
    - I also want to allow the user to struggle to disable DND. Make them disable it 5-10 times
      before the app finally gives in and lets them have their way. This means a lot of work in one
      class, or some genius level code.


#### Progress update: 19.06.20

Okay so now I'm getting the hang of writing test cases, I have written some pretty kick ass RxJava,
the java.time API is awesome. Things are looking up. What I need to do next is write test cases
for input of un/lock data to the database. And all the edge cases there. That foundation gotta be
really strong and handle the case for many time zones and all.
Then, write that automater for setting DND that's on as often as possible. The logic has to be solid
there.
Also, I watched a video on similar apps and then realized how our mind makes associations on where
the icon for the app is and so it's a reflexive action to open our beloved apps. So, when we're over
using, one thing I can do is show UI that doesn't show up the same every time. So, it isn't only a
button which shows up in random locations but possibly also require a bit more effort like do a
calculation, use four fingers or something more involved. That way it isn't just a reflex, but you
need some more thinking.

Do I think all of this is worth doing? Hell yeah. I'm using my phone way more than I should.

#### Progress update: 28.06.20

I am stuck at a strange place. I want my code to be reactive, but I also need some parts of it to
be poll based.
DND state changes can happen in two places: the user clicks on it by themself, or I do it through
code. I can track the change by polling the DND state every second.
If I set state from code, then I know I did that from code. But, I will also have to transfer this
info to DndPoller.
Okay, let's just list out what DndPoller should do: track Dnd state and emit whenever there is a
change. This emitted event should also contain if the change was done by the user or by our code.

#### Progress update: 09.07.20

Argh I am really stuck at understanding what the architecture should be like.

View        <-> Activity
ViewModel   <-> UseCase
Model       <-> Repository and lower

But now where do classes that do specific work go? For example setting phone's DND.
They're kinda like utility classes but not really. Argh.
Alright, I'm calling them Managers. Fuck it. Manager. Ah I dislike that word for this. But it does
fit the most. Worker sounds like something to with threads.

#### Progress update: 16.07.20

Had a great idea: there shouldn't be two buttons, but just one that opens from the bottom. Whenever
you wanna be in social mode, that should be effort. But for everything else, it should be easy to
go back to doing what you were gonna do normally. I know there's an extra step, but I think this
will work a bit better than having to choose between Social And Focussed mode.
Anywaaaaay. Also got the intent chooser activity to show up nicely which looks pretty great IMO.

Also there has to be a difference between the phone setting focus mode and the user setting it.
Just putting the thought out there.

Now the next step is going to be to make this bottom dialog open smartly:

- when you've unlocked too many times in the last x minutes
- when you've used your phone for more than x minutes in the last 30 minutes
- when you've used social for more than x minutes in the last 3 hours
- it should definitely not show up when I haven't used my phone in a while. Coooooool.

Also, that thing gotta avoid letting me have muscle memory to just disable it. Need ideas there.
This is also why I will have to differentiate when the phone enforces Focus Mode, and when the user
requests it. Cuz I want myself to have no option in the first hour of the morning to even choose to
use social media stuff, and so the dialog shouldn't even open then. But then afterwards it should.
Hmm actually can I do it without that?