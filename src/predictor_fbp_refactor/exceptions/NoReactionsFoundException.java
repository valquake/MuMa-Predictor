package predictor_fbp_refactor.exceptions;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author valer
 */
public class NoReactionsFoundException extends Exception {
    public NoReactionsFoundException(String errorMessage) {
        super(errorMessage);
    }

    NoReactionsFoundException(int i) {
        super(Integer.toString(i));
    }
}
