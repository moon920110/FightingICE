import java.util.concurrent.CountDownLatch;

public class MultiTheadPlayout implements Runnable {
    private Node node;
    private CountDownLatch countDownLatch;

    public MultiTheadPlayout(Node child, CountDownLatch countDownLatch) {
        node = child;
        this.countDownLatch = countDownLatch;
    }

    public void run() {
        try {
            double score = 0;
            score = node.playout();
            node.setScore(score);
            node.addGames();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            countDownLatch.countDown();
        }
    }
}