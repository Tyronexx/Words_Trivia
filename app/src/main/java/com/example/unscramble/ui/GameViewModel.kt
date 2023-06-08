package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update



class GameViewModel:ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()


/**  currentWord of the type String to save the current scrambled word*/
    private lateinit var currentWord: String

/**  property to serve as a mutable set to store used words in the game.*/
    private var usedWords: MutableSet<String> = mutableSetOf()

    /**Picks a random word from WordsData and shuffles it*/
    private fun pickRandomWordAndShuffle():String{
        // Continue picking up a new random word until you get one that hasn't been used before
        currentWord = allWords.random()
        if (usedWords.contains(currentWord)){
            return pickRandomWordAndShuffle()
        }else{
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }
/**  method to shuffle the current word called that takes a String and returns the shuffled String.*/
    private fun shuffleCurrentWord(word: String):String{
        val tempWord = word.toCharArray()

        //Scramble the word
        tempWord.shuffle()
        while (String(tempWord).equals(word)){
            tempWord.shuffle()
        }

        return String(tempWord)
    }

/**  function to initialize the game*/
    fun resetGame(){
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambeledWord = pickRandomWordAndShuffle())
    }
//  The code inside the init block is the first to be executed when the class is instantiated.
    init {
        resetGame()
    }


/**  this state tracks the user guess*/
    var userGuess by mutableStateOf("")
        private set
/**  this updates user guess*/
    fun updateUserGuess(guessedWord: String){
        userGuess = guessedWord
    }

/**  method to verify the word a user guessed and then either update the game score or display an error*/
    fun checkUserGuess(){
        if (userGuess.equals(currentWord, ignoreCase = true)){
            // User's guess is correct, increase the score
            // and call updateGameState() to prepare the game for next round
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)
        }else{
            //Users guess is wrong, show an error
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
    //Reset user guess (textField value)
    updateUserGuess("")
    }


/**  this will update the score, increment the current word count and pick a new word from the WordsData.kt file
    this prepares the game for next round*/
    fun updateGameState(updatedScore: Int){
        if (usedWords.size == MAX_NO_OF_WORDS){
            //Last round of game, update isGameOver to true, don't pick a new word and scramble
            _uiState.update { currentState ->
                currentState.copy(
//                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        }else{
            //Normal round of game
            _uiState.update { currentState ->
                currentState.copy(
//                    isGuessedWordWrong = false,
                    currentScrambeledWord = pickRandomWordAndShuffle(),
                    score = updatedScore,
                    currentWordCount = currentState.currentWordCount.inc(),
                )
            }
        }
    }

    /**Skips a word*/
    fun skipWord(){
        updateGameState(_uiState.value.score)
        //reset user guess(pass an empty string into guessed word in order to skip it)
        updateUserGuess("")
    }
}

