##Project Charlie
Everyone probably knows about the famous winning hand, Blackjack, Ace+10.
Then, there's the less famous, less probable but more profitable
five-card Charlie which is a hand of five cards that does not break.

Name after that winning hand,
Project Charlie is an extensible, actor-based Blackjack system for teaching purposes.
Charlie is extensible in the sense that parts of it are built on plug-in modules that can be programmed
without having to rebuild the entire system. Charlie uses [actors](http://en.wikipedia.org/wiki/Actor_model)
as its model of distributed computing. In other words, Charlie is a client-server system
that runs on multiple hosts and can exploit hyper-threaded, multicore processors.

Games are
powerful tools in their own right for teaching; in fact, when playing games or in this case developing them,
blurs the line between learning and playing. Are we learning or playing?
Let others decide.

Blackjack is an example of an excellent game design.
Namely, it is easy to play yet difficult to master.

###What could Charlie teach?

* History
* Game theory
* Utility theory and risk taking
* Probability theory, statistics
* Concurrency and threads
* Hyper-threading concepts
* Synchronization and communication
* Actors
* Network programming
* Serialization
* User interface design
* Artificial intelligence
* Object-oriented principles
* Animation
* Version control
* Server design
* Software testing
* Diagnostic logging
* Even markdown (in the case of this document)
* How to look like you know what you're doing should you get stuck in Vegas

The rest of this document describes the Charlie design and how to extend it.
I will not describe in detail the rules Blackjack.
Readers that find lots of Blackjack info in books and/or the Internet.
I will mention, however, not only _how_ to play Blackjack but also how
bets.

###A brief history of Blackjack
The roots of Blackjack roots go back to Don Quixote and Seville and the 17th century.
Serious study of Blackjack began in the 1950s with long-running computer simulations to discover what is
called the _Basic Strategy_, about which we shall have much to say. By the 1960s, E.O. Thorp, the famous
MIT professor, published, _Beat the Dealer_, which for a while caused casinos to change the game design
to counteract the methods described therein. However, the casino tactics slowed down the game play and customers
stopped coming. It forced casinos to go back to simpler rules and look for other means to thwart
the Basic Strategy and card counting. Peter Griffin published _The Theory of Blackjack_ in the late 1990s. It laid
out what some might regard as the definitive mathematical treatment of Blackjack and player opportunities.
In 2003, Ben Mezrich, _Bringing Down the House_, published a true story of the exploits of students
and their MIT professor in Las Vegas. The story was dramatized by the popular
2008 movie, _21_.
In my own case, I studied Blackjack indepth, asking whether a machine could learn to play
and count cards. The published papers are [here](http://foxweb.marist.edu/users/ron.coleman/).
In 2010, I developed a related
system in [Scala](www.scala-lang.org). However, having to teach Scala, too,
in addition to teaching some Blackjack, proved to be too much of a distraction. Thus, the 
goal here is to focus more on Blackjack and Java.

###Basic ideas
Charlie is uses a client-server architecture organized around the model view controller (MVC) design pattern.
After a "real" player logs in and establishes a connection, an instance of *House* constructs
an instance of *Dealer* for the player. The player is bound to this *Dealer* until the player
logs out.

Games are multiplayer except real players do not play one another.  
Instead, depending on the configuration,
*Dealer* may allocate bots that simulate real players.
If no bots have been configured, the game is "heads up," that is, the player
against *Dealer*.

The Dealer keeps a copy of the hands and implements the play rules, e.g., determining
the sequence of players, executing play
requests, deciding wins, losses, etc.
The Dealer broadcasts the game state to all players. For instance, when the Dealer
deals a card, it is sent to all players, even if the hand is not for a given player.
This is so that all players can "see" the table and if necessary, count cards.
The player's job is to render this information. If a player receives Ace+10, this
is of course a Blackjack.
However, the player doesn't have to determine this. Dealer
broadcasts "blackjack" to all players.

A key design feature is hands are not passed around among player.
Instead, Charlie uses _hand ids_.
A hand id is a unique key for a hand.
Each player has one or more hands, as far as the dealer is concerned.
Thus, when Dealer hits a hand, it sends a Card object and a hand id.
If the corresponding hand does not belong to a given player, the player
can ignore the card. If however the hand id corresponds to a hand
a player owns, the player has to respond. The permissible responses are
hit, stay, double-down, surrender, and in theory, split which is not
yet fully implemented.

If, of course, the player busts on a hit or double-down, there is
no permissible request. Game is over for that player.
In that case, Dealer tells the player it has broke so the player
just needs to wait for Dealer before making the next player.

Dealer only deals with instances of *IPlayer*, a Java interface.
Thus, Dealer mostly doesn't know or care if IPlayer is a real player or a bot.
The exception is when placing bets. Dealer starts a new game only when 
Dealer receives a bet from *RealPlayer* which is an imiplementation of IPlayer.
However, RealPlayer could also be a bot. It's just in practice RealPlayer
is associated with a "real" player on the client.

Bots implement *IBot*, sub-interface of IPlayer.
IBot instances run on the server.
Real player bots, that is,
bots that play and bet and run on the client, implement *IArtificialPerson*, 
a sub-interface of IPlayer.

These are the basic ideas.
 
###Config file
There is a system-wide properties file: _charlie.props_.
It configures several important parameters.

###Shoes
The first thing to know before developing bots is how to control the cards which
come from a _shoe_.
There is a property, _charlie.shoe_. The value must be a fully qualified
subclass of *Shoe*.
You then just need to add cards to *cards* which is a *List<Card>*.
