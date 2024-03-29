package org.jboss.as.quickstarts.numberguess;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

/**
 * <p>
 * {@link Game} contains all the business logic for the application, and also serves as the
 * controller for the JSF view.
 * </p>
 * <p>
 * It contains properties for the <code>number</code> to be guessed, the current <code>guess</code>,
 * the <code>smallest</code> and <code>biggest</code> numbers guessed so far (as this is a
 * higher/lower game we can prevent them entering numbers that they should know are wrong), and the
 * number of <code>remainingGuesses</code>.
 * </p>
 * <p>
 * The {@link #check()} method, and {@link #reset()} methods provide the business logic whilst the
 * {@link #validateNumberRange(FacesContext, UIComponent, Object)} method provides feedback to the
 * user.
 * </p>
 * 
 * @author Pete Muir
 * 
 */

@ManagedBean
@SessionScoped
public class Game implements Serializable {

   private static final long serialVersionUID = 991300443278089016L;

   /**
    * The number that the user needs to guess
    */
   private int number;

   /**
    * The users latest guess
    */
   private int guess;

   /**
    * The smallest number guessed so far (so we can track the valid guess range).
    */
   private int smallest;

   /**
    * The largest number guessed so far
    */
   private int biggest;

   /**
    * The number of guesses remaining
    */
   private int remainingGuesses;

   /**
    * The maximum number we should ask them to guess
    */
  
   private java.util.Random random = new java.util.Random(System.currentTimeMillis());

   private int maxNumber = 100;

   java.util.Random getRandom() {
      return random;
   }

   /**
    * The random number to guess
    */
   /*@Inject
   @Random
   Instance<Integer> randomNumber;*/
   int randomNumber;

   public Game() {
   }

   public int getNumber() {
      return number;
   }

   public int getGuess() {
      return guess;
   }

   public void setGuess(int guess) {
      this.guess = guess;
   }

   public int getSmallest() {
      return smallest;
   }

   public int getBiggest() {
      return biggest;
   }

   public int getRemainingGuesses() {
      return remainingGuesses;
   }

   /**
    * Check whether the current guess is correct, and update the biggest/smallest guesses as needed.
    * Give feedback to the user if they are correct.
    */
   public void check() {
      if (guess > number) {
         biggest = guess - 1;
      } else if (guess < number) {
         smallest = guess + 1;
      } else if (guess == number) {
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Correct!"));
      }
      remainingGuesses--;
   }

   /**
    * Reset the game, by putting all values back to their defaults, and getting a new random number.
    * We also call this method when the user starts playing for the first time using
    * {@linkplain PostConstruct @PostConstruct} to set the initial values.
    */
   @PostConstruct
   public void reset() {
      this.smallest = 0;
      this.guess = 0;
      this.remainingGuesses = 10;
      this.biggest = maxNumber;
      this.number = getRandom().nextInt(maxNumber - 1) + 1;
   }

   /**
    * A JSF validation method which checks whether the guess is valid. It might not be valid because
    * there are no guesses left, or because the guess is not in range.
    * 
    */
   public void validateNumberRange(FacesContext context, UIComponent toValidate, Object value) {
      if (remainingGuesses <= 0) {
         FacesMessage message = new FacesMessage("No guesses left!");
         context.addMessage(toValidate.getClientId(context), message);
         ((UIInput) toValidate).setValid(false);
         return;
      }
      int input = (Integer) value;

      if (input < smallest || input > biggest) {
         ((UIInput) toValidate).setValid(false);

         FacesMessage message = new FacesMessage("Invalid guess");
         context.addMessage(toValidate.getClientId(context), message);
      }
   }
}
