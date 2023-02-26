package com.wriesnig;

import com.wriesnig.api.git.DefaultGitUser;
import com.wriesnig.db.expertise.ExpertiseDatabase;
import com.wriesnig.db.stack.StackDatabase;
import com.wriesnig.expertise.User;
import com.wriesnig.expertise.git.GitExpertiseJob;
import com.wriesnig.expertise.stack.StackExpertiseJob;
import com.wriesnig.utils.AccountsFetcher;
import com.wriesnig.utils.Logger;
import com.wriesnig.gui.Observable;
import com.wriesnig.gui.Observer;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CharacterizerApplication implements Observable {
    private ArrayList<Observer> observers = new ArrayList<>();
    private ArrayList<Integer> ids;
    private final AccountsFetcher accountsFetcher;

    public CharacterizerApplication(ArrayList<Integer> ids) {
        //22656,157882,139985,57695,203907,571407,922184,70604,1221571,276052,829571,21234,100297

        this.ids = ids;
        accountsFetcher = new AccountsFetcher();
    }

    public void run() {
        Logger.info("Running characterizer application.");
        ArrayList<User> users = accountsFetcher.fetchMatchingAccounts(ids);
        runExpertiseJobs(users);
        storeUsersExpertise(users);
        notifyObservers(users);
    }

    public void runExpertiseJobs(ArrayList<User> users){
        Thread stack = new Thread(()->runStackExpertiseJobs(users));
        Thread git = new Thread(()->runGitExpertiseJobs(users));

        stack.start();
        git.start();

        try {
            stack.join();
            git.join();
        } catch (InterruptedException e) {
            Logger.error("Joining expertise job threads failed. ", e);
        }
    }


    private void runStackExpertiseJobs(ArrayList<User> users) {
        Logger.info("Running stack-expertise job.");
        StackDatabase.initDB();
        for (User user : users) {
            new StackExpertiseJob(user).run();
        }
    }

    private void runGitExpertiseJobs(ArrayList<User> users) {
        Logger.info("Running git-expertise job.");
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (User user : users) {
            if(!(user.getGitUser() instanceof DefaultGitUser))executorService.execute(new GitExpertiseJob(user));
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            Logger.error("Expertise thread was interrupted.", e);
        }
    }


    public void storeUsersExpertise(ArrayList<User> users){
        ExpertiseDatabase.initDB();
        for(User user: users){
            ExpertiseDatabase.insertUser(user);
        }
    }

    @Override
    public void addObserver(Observer observer) {
        this.observers.add(observer);
    }

    private void notifyObservers(ArrayList<User> users){
        for (Observer observer : this.observers) {
            observer.notifyUpdate(users);
        }

    }
}
