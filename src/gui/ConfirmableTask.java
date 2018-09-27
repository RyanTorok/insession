package gui;

public class ConfirmableTask {

    private String option;
    private Runnable task;

    public ConfirmableTask(String option, Runnable task) {
        this.option = option;
        this.task = task;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public Runnable getTask() {
        return task;
    }

    public void setTask(Runnable task) {
        this.task = task;
    }
}
