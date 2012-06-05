package it.pdm.project.MusicPlayer.utils;

public class Utilities {
	
	//Conversione del tempo da millisecondi a Stringa formattata tipo hh:mm:ss o mm:ss
	public String milliSecondsToTimer(int milliseconds){
        String finalTimerString = "";
        String secondsString = "";
 
        // Convert total duration into time
           int hours = (int)( milliseconds / (1000*60*60));
           int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
           int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
           // Add hours if there
           if(hours > 0){
               finalTimerString = hours + ":";
           }
 
           // Prepending 0 to seconds if it is one digit
           if (seconds < 10)
               secondsString = "0" + seconds;
           else
               secondsString = "" + seconds;
 
           finalTimerString = finalTimerString + minutes + ":" + secondsString;
 
        // return timer string
        return finalTimerString;
    }
 
	//Metodo ausiliario per calcolare la percentuale a partire da valore corrente e valore massimo
	public int getProgressPercentage(int currentDuration, int totalDuration){
        Double percentage = (double) 0;
 
        int currentSeconds = (int) (currentDuration / 1000);
        int totalSeconds = (int) (totalDuration / 1000);
 
        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;
 
        // return percentage
        return percentage.intValue();
    }

	//Metodo ausiliario per calcolare la posizione corrente a partire dalla percentuale e dal valore massimo
    public static int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);
 
        // return current duration in milliseconds
        return currentDuration * 1000;
    }
}
