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

Charlie games are multiplayer except real players do not play one another.  
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

A key design feature is hands themselves are not passed around among players
or over the network.
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
Dealer receives a bet from *RealPlayer* which is an implementation of IPlayer.
However, RealPlayer could also be a bot. It's just in practice RealPlayer
is associated with a "real" player on the client.

###Plugins
Charlie has five (5) types of plugins:
1. Shoes
2. B9 bots
3. N6 bots
4. Rachel bots
5. Side bets

There is a system-wide properties file: _charlie.props_. It contains plugin
declarations.

###Shoes
A _shoe_ contains instances of *Card* objects from which Dealer deals to players.
A shoe must implement the behaviors give by *IShoe*.
There is a concrete class, *Shoe*, which implements IShoe.
Shoe has six randomly shuffled decks for "serious" play and/or training.
In general, however, an IShoe may contain as many or as few cards
as necessary. Thus, shoes are very useful for debugging purposes.

There is a property, _charlie.shoe_ in charlie.props. The value must be a fully qualified
subclass of *Shoe*. Here's an example

    charlie.shoe charlie.card.Shoe01

It turns out the *Shoe01* is a one-deck shoe which I've found helpful for testing.

When Dealer starts, it looks for this property and constructs a _charlie.card.Shoe00_.
You then just need to add cards to *cards* which is a *List<Card>*.
Dealer then uses this shoe.
If you use your own shoe, you just have to make sure the class is in the Charlie project
class path via a jar file or source in the IDE.

###Cards
There are two types of cards: *Card* and *ACard*.
Card is used
by the controller (i.e., the server) and view (i.e., the client)
to implement the play rules.

Card objects have rank and suit.

The following snippet constructs a three of spades:

    Card card = new Card(3, Card.Suit.SPADES)

Here's how to make an Ace of spades:

    Card card = new Card(Card.ACE, Card.Suit.SPADES)

Once you have a Card, you add it to the shoe as follows:

    cards.add(card)

Card has various methods to inquire about itself, like its value,
whether it is a face card (J<, K, Q), an Ace, etc.

ACard is the "animated" analog of Card.
It is a subclass of *Sprite*. ACard objects
move around the table and have front and back faces.
They are in many way more sophisticated than a Card.
They key things to know are that an ACard constructs
itself from a Card, has a home position on the table, and a current position
on the table. ACard always seeks its home position starting
from wherever it is on the table. The card motion is along a Euclidena
straight line.

###B9 & N6 bots
These not only act like a player.
They implement *IBot* which is a sub-interface of IPlayer.
In other words, they implement IPlayer.
As far as Dealer is concerned,
they are players.
The only thing is they run on the server, not the client.
This means they have special access to the server and
can crash the server.

You specify these bots in the charlie.props file with the
keys _charlie.bot.b9_ and _charlie.bot.n6_ respectively.
The key must declare the fully qualified class names.

On the table, Dealer plays B9 in the left seat and N6, the right seat.
RealPlayer is in the middle or heads-up seat.
 
Dealer implicitly assumes IPlayer instances are an independent
threads. What does this mean?
An IBot must create its own thread to invoke Dealer.
For instance, IBot defines through IPlayer the behavior, _play_.
Dealer invokes this method only once on IBot when it is the
player's turn.
IBot needs to respond by invoking _hit_, _stay_, etc. on Dealer.
However, it cannot invoke these methods on Dealer
in the same thread that's running
the play method.
IBot must instead spawn (or wakeup) a worker thread and return to Dealer which is
waiting passively for a response.
The worker thread then can invoke methods Dealer.

I will just point out that if IBot, in its worker thread,
invokes _hit_, Dealer responds in turn by invoking _deal_ on IBot.
Here again, IBot must use a worker thread to respond to the
new card.

For all practical purposes, B9 and N6 bots are identical from Dealer's point of
view. The only difference is seating as I mentioned above.
The intent of having these two bots was to also employ different play
strategies.
For instance, B9 might use the [Wizard of Odds](http://wizardofodds.com/games/blackjack/)
21 cell strategy where N6 might use the 420 cell strategy.
BTW, the Dealer does not, at the moment, support splits and that fact cuts down
on the number of cells on both cases.

###Rachel bots
These bots implement *IRachel* which is a sub-interface of IPlayer.
Rachel bots run with a view on the client-side.
They are intended to implement the most sophisticated play and bet strategies
to maximize player returns.
It might be best, for Rachel, to start with the 420 cell play strategy
and stay with a balanced, level-one system like the [Hi-Lo](http://en.wikipedia.org/wiki/Card_counting) 
or unbalanced, level-one
system like [Knock Out](http://www.koblackjack.com/).

Rachel play like a human. That is, it must appear to think before making a decision.
Otherwise, things will happen too fast and we won't be able to see or know
what it really did or why.

When Rachel is in charge, it must also behave since otherwise it will crash
the client.

Here are the steps to starting a game a bet:

1. Send the clear message to the table.
2. Get the wager from the table. If there isn't a wager, then use the money manager, _click_
to create one. (Note: you'll have to get the chips from the money manager and use the
chip coordinates to select the amount.
The chips are 100, 25, and 5 from left to right on the table.)
3. Invoke the *Courier* to send the bet to Dealer. The returned Hid is the hand
id for the hand.
4. Wait for _startGame_. At this point Rachel can only observe the game until 
it is its turn.
This is an opportunity for Rachel to count cards since every card is sent
to all players bound for the respective hand identified by the hand id.
6. When Rachel receives _play_, it must respond with hit, double-down, or stay.
Hits arrive via the _deal_ message.
7. After Rachel stays, busts, gets a blackjack, or Charlie,
it must wait for _endGame_ when the game is over.
8. Go to step 1.

To play a double-down, Rachel does the following:

1. Invoke _dubble_ on the hand id. This doubles the bet in the hand.
2. Invoke _dubble_ on Courier. This send the play to the Dealer.
3. Invoke _dubble_ on the table. This doubles the wager on the table.

Of course, after double-down,  Rachel is done for the game and just waits
for endGame.



###Side bets


