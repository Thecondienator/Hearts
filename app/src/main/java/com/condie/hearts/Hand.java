package com.condie.hearts;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class Hand extends AppCompatActivity {

    /* CONSTANTS */
    private enum Suit {SPADES, HEARTS, DIAMONDS, CLUBS}
    private enum Rank {ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING}

    /* VARIABLES */
    private Deck deck;
    private PlayerHand[] playerHands;
    private int curPlayerIndex;

    /* PAGE OBJECTS */
    private Button btDeal;
    private Button btReset;
    private TextView tvHeader;
    private TextView[] tvPlayerCards;

    /* For top action bar */
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    /* For side nav */
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hand);

        /* Copy/pasted code, analyze later */
        mDrawerList = (ListView)findViewById(R.id.navList);
        addDrawerItems();
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mActivityTitle = getTitle().toString();
        setupDrawer();

        btDeal = (Button) findViewById(R.id.btDeal);
        btReset = (Button) findViewById(R.id.btReset);
        tvHeader = (TextView) findViewById(R.id.tvHeader);
        tvPlayerCards = new TextView[4];
        {
            tvPlayerCards[0] = (TextView) findViewById(R.id.tvP1Cards);
            tvPlayerCards[1] = (TextView) findViewById(R.id.tvP2Cards);
            tvPlayerCards[2] = (TextView) findViewById(R.id.tvP3Cards);
            tvPlayerCards[3] = (TextView) findViewById(R.id.tvP4Cards);
        }

        // TODO find out why spamming this button makes it go faster
        btDeal.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // TODO clean
                for (TextView tv : tvPlayerCards)
                {
                    tv.setText("");
                }
                tvHeader.setText("Dealing...");
                dealCards();
            }
        });

        btReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tvHeader.setText("Nothing happened :(");
            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().hide();
                //getSupportActionBar().setTitle("Navigation!");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().show();
                //getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    private void addDrawerItems() {
        final String[] osArray = { "Game", "Stats", "Logout"};
        //This throws an ArrayIndexOutOfBounds exception -- why?
        //osArray[3] = "Another item";
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //What's the proper way to do this?  I know this is susceptible to bounds errors
                String toastText = osArray[position];
                Toast.makeText(Hand.this, toastText, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hand, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        /* Added for the navigation drawer */
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Method to distribute 13 random cards to each player, to initialize the game
    public void dealCards()
    {
        // Initialize deck and randomize
        deck = new Deck();
        deck.shuffle();

        // Initialize player hands
        playerHands = new PlayerHand[4];
        for (int i = 0; i < playerHands.length; i++)
        {
            playerHands[i] = new PlayerHand();
        }

        // TODO wtf is UI thread (vs background thread)?  See if Handler/Runnable is better
        curPlayerIndex = 0;
        // Deal until there are no cards
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                if (dealCard())
                {
                    handler.postDelayed(this, 50);
                }
                // Done dealing
                else
                {
                    tvHeader.setText("Good luck!");
                }
            }
        };
        runnable.run();
    }

    // Deal one card from deck to current player
    private boolean dealCard()
    {
        if (deck.isEmpty())
        {
            return false;
        }
        // Draw a card and add to player's hand
        Card card = deck.drawCard();
        playerHands[curPlayerIndex].addCard(card);

        // Update the text on the page
        tvPlayerCards[curPlayerIndex].setText(playerHands[curPlayerIndex].toString());

        // Update to next player
        curPlayerIndex = (curPlayerIndex + 1) % 4;

        return true;
    }

    // Object to represent a full 52-card deck
    // cards MUST BE INITIALIZED IN ALL CONSTRUCTORS
    public class Deck
    {
        private ArrayList<Card> cards;

        public Deck()
        {
            cards = new ArrayList<>(52);

            // Initialize to have one of every card
            for (Suit suit : Suit.values())
            {
                for (Rank rank : Rank.values())
                {
                    cards.add(new Card(suit, rank));
                }
            }
        }

        // Shuffle the cards up before dealing
        public void shuffle()
        {
            Collections.shuffle(cards);
        }

        // Remove top card in the list and return it
        public Card drawCard()
        {
            if (cards.size() > 0)
            {
                return cards.remove(0);
            }
            else
            {
                return null;
            }
        }

        // boolean, returns if the deck is empty or not
        public boolean isEmpty()
        {
            if (cards.size() > 0)
            {
                return false;
            }
            return true;
        }
    }

    public class PlayerHand
    {
        private ArrayList<Card> cards;

        public PlayerHand()
        {
            cards = new ArrayList<>(13);
        }

        // Add a single card to this player's hand
        public void addCard(Card card)
        {
            cards.add(card);
        }

        // Print out the card values as a string (test purposes)
        public String toString()
        {
            StringBuilder output = new StringBuilder();
            for (Card card : cards)
            {
                output.append(card.rank);
                output.append(" of ");
                output.append(card.suit);
                output.append("\n");
            }
            return output.toString();
        }

        // Removes and returns the Card object at the param index
        public Card playCard(int index)
        {
            if (cards.size() > index)
            {
                return cards.remove(index);
            }
            else
            {
                return null;
            }
        }
    }

    // Object to represent a single card
    public class Card
    {
        private Suit suit;
        private Rank rank;

        public Card(Suit suit, Rank rank) {

            this.suit = suit;
            this.rank = rank;
        }
    }

    // TODO: initial card suit, determine winner, attach cards to player
    public class Round
    {

        public Round()
        {

        }
    }
}