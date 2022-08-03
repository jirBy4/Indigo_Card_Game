package indigo
import kotlin.system.exitProcess

val pointCards = mutableListOf(
    "A♣", "K♣", "Q♣", "J♣", "10♣", "A♠", "K♠", "Q♠", "J♠", "10♠",
    "A♥", "K♥", "Q♥", "J♥", "10♥", "A♦", "K♦", "Q♦", "J♦", "10♦")
var lameCards = mutableListOf(
    "9♣", "8♣", "7♣", "6♣", "5♣", "4♣", "3♣", "2♣",
    "9♠", "8♠", "7♠", "6♠", "5♠", "4♠", "3♠", "2♠",
    "9♥", "8♥", "7♥", "6♥", "5♥", "4♥", "3♥", "2♥",
    "9♦", "8♦", "7♦", "6♦", "5♦", "4♦", "3♦", "2♦")
var unusedCards = mutableListOf<String>(); var tableCards = mutableListOf<String>()
var playerYard = mutableListOf<String>(); var botYard = mutableListOf<String>()
var playerHand = mutableListOf<String>(); var botHand = mutableListOf<String>()
var firstPlay = String(); var turnOrder = -1

fun main() {
    println("Indigo Card Game")
    val indigo = Game()
    if(indigo.playerOrder() == "yes") {
        turnOrder = 0; firstPlay = "Player" }
    else {
        turnOrder = 1; firstPlay ="Computer" }
    indigo.openingMsg()
    while (unusedCards.size + playerHand.size + botHand.size != 0) {
        indigo.tableReader()
        if (turnOrder % 2 == 0) indigo.playerMove() else indigo.botMove()
        turnOrder++; println() }
    indigo.finalScore()
    println("Game Over")
}

class Game {
    private var topCard = " "; private var choice = " "; private var lastWinner = String()
    private var playerScore = 0; private var botScore = 0; private var tablePoints = 0

    fun playerOrder(): String {
        println("Play first? ")
        choice = readln().lowercase()
        return when (choice) {
            "exit" -> exitProcess(1)
            "no", "yes" -> choice
            else -> playerOrder() }
    }

    fun openingMsg() {
        unusedCards += pointCards + lameCards; unusedCards.shuffle()
        botHand = handRefresh() as MutableList<String>; playerHand = handRefresh() as MutableList<String>
        tableCards = unusedCards.take(4) as MutableList<String>; unusedCards.removeAll(unusedCards.take(4))
        tableCards.forEach { i -> if (i in pointCards) tablePoints++}
        topCard = tableCards[tableCards.lastIndex]
        println("Initial cards on the table: ${tableCards.joinToString(" ")}\n") }

    fun tableReader() = if (tableCards.size == 0 ) println("No cards on the table") else println("${tableCards.size} cards on the table, and the top card is ${tableCards[tableCards.lastIndex]}")

    private fun tableReset() {
        topCard = " "; tablePoints = 0; tableCards.clear() }

    private fun handRefresh() = unusedCards.take(6).also { unusedCards.removeAll(it) }

    fun playerMove() {
        if (playerHand.size == 0 && unusedCards.size == 0) return
        if(playerHand.size == 0 && unusedCards.size >= 6) playerHand = handRefresh() as MutableList<String>
        println("Cards in hand: ")
        for (i in 0 until playerHand.size) {
            print("${i +1})${playerHand[i]} ") }
        moveChoice()
    }

    private fun moveChoice() {
        println("\nChoose a card to play (1-${playerHand.size}): ")
        choice = readln()
        if (choice == "exit") {
            println("Game Over"); exitProcess(1)}

        when {
            choice.toIntOrNull() == null -> moveChoice()
            choice.toInt() !in 1..playerHand.size -> moveChoice()
            else -> {
                tableCards += playerHand[choice.toInt() - 1]; winCheck(playerHand[choice.toInt() - 1], "Player")
                playerHand.removeAt(choice.toInt() - 1) }
        }
    }

    private fun suitCheck (hand: MutableList<String>) = if (!hand.any { it.last() == topCard.last() }) "empty" else hand.filter { it.last() == topCard.last() }.random()

    private fun numCheck (hand: MutableList<String>) = if (!hand.any{ it[0] == topCard[0] }) "empty" else hand.filter { it[0] == topCard[0] }.random()

    fun botMove() {
        if (botHand.size == 0 && unusedCards.size == 0) return
        if (botHand.size == 0 && unusedCards.size >= 6) botHand = handRefresh() as MutableList<String>
        println(botHand.joinToString(" "))
        val cardPlayed = botCheck(); println("Computer plays $cardPlayed")
        tableCards += cardPlayed; winCheck(cardPlayed, "Computer"); botHand.removeAt(botHand.indexOf(cardPlayed)) }

    private fun botCheck(): String = when {
        botHand.size == 1 -> botHand[0]
        tableCards.size == 0 -> botMoveChoice()
        suitCheck(botHand) != "empty" -> suitCheck(botHand)
        numCheck(botHand) != "empty" && suitCheck(botHand) == "empty" -> numCheck(botHand)
        else -> botMoveChoice() }

    private fun botMoveChoice(hand: MutableList<String> = botHand): String {
        var suitCount = 0; var maxSuit = ' '; var numCount = 0; var maxNum = ' '
        for (i in mutableListOf('♠', '♥', '♣', '♦')) {
            if (hand.count {it.last() == i} > suitCount ) {
                maxSuit = i; suitCount = hand.count { it.last() == i }
            }
        }
        for (j in mutableListOf('1', '2', '3', '4', '5', '6', '7', '8', '9', 'J', 'Q', 'K')) {
            if (hand.count { it.first() == j } > numCount) {
                maxNum = j; numCount = hand.count { it.first() == j }
            }
        }
        val choice = when {
            suitCount >= numCount -> botHand.filter {it.last() == maxSuit }.random()
            numCount > 1 && suitCount < numCount -> botHand.filter {it[0] == maxNum }.random()
            else -> botHand.random() }
        return choice
    }

    private fun winCheck(card: String, winner: String) {
        if (card in pointCards) tablePoints++

        if (topCard != " " && card[card.lastIndex] == topCard[topCard.lastIndex] || card[0] == topCard[0]) {
            lastWinner = winner; println("$lastWinner wins cards"); scoreAdd(lastWinner); score() }
        else {
            topCard = tableCards[tableCards.lastIndex] }
    }

    private fun scoreAdd(winner: String) {
        if (winner == "Player") {
            playerScore += tablePoints; playerYard += tableCards; tableReset() }
        else {
            botScore += tablePoints; botYard += tableCards; tableReset() }
    }

    private fun score() = println("Score: Player $playerScore - Computer $botScore\nCards: Player ${playerYard.size} - Computer ${botYard.size}")

    fun finalScore() {
        tableReader()
        if (tableCards.size != 0 ) scoreAdd(lastWinner)
        tablePoints += 3
        when {
            playerYard.size > botYard.size -> scoreAdd("Player")
            playerYard.size < botYard.size -> scoreAdd("Computer")
            else -> scoreAdd(lastWinner) }
        score()
    }
}