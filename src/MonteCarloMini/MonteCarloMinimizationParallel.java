package MonteCarloMini;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

class MonteCarloMinimizationParallel extends RecursiveTask<Integer> {
    public static int CUTOFF = 1000;

    int rows, columns; // grid size
    int low;
    int high;
    static int min = Integer.MAX_VALUE;
    // int local_min = Integer.MAX_VALUE;

    static int finder = -1;

    SearchParallel[] searches; // Array of searches

    static long startTime = 0;
    static long endTime = 0;

    // timers - note milliseconds
    private static void tick() {
        startTime = System.currentTimeMillis();
    }

    private static void tock() {
        endTime = System.currentTimeMillis();
    }

    public MonteCarloMinimizationParallel(int low, int high, SearchParallel[] searches) {
        this.low = low;
        this.high = high;
        this.searches = searches;
    }

    @Override
    protected Integer compute() {

        // int min = Integer.MAX_VALUE;
        int local_min = Integer.MAX_VALUE;

        // int finder = -1;

        if ((high - low) < CUTOFF) {

            for (int i = low; i < high; i++) {
                local_min = searches[i].find_valleys();
                if (local_min < min) { // don't look at those who stopped because hit
                                       // exisiting path
                    min = local_min;

                    finder = i;

                }
                return finder;
            }

        } else {

            int mid = (high + low) / 2;

            MonteCarloMinimizationParallel rightTask = new MonteCarloMinimizationParallel(low, mid, searches);

            MonteCarloMinimizationParallel leftTask = new MonteCarloMinimizationParallel(mid, high, searches);

            rightTask.fork();

            int leftAnswer = leftTask.compute();

            int rightAnswer = rightTask.join();

            if (searches[leftAnswer].get_height() < searches[rightAnswer].get_height()) {

                return leftAnswer;

            }

            return rightAnswer;

        }
        return finder;
    }

    public static void main(String[] args) {

        int rows = Integer.parseInt(args[0]);
        int columns = Integer.parseInt(args[1]);
        Double xmin = Double.parseDouble(args[2]);
        Double xmax = Double.parseDouble(args[3]);
        Double ymin = Double.parseDouble(args[4]);
        Double ymax = Double.parseDouble(args[5]);
        Double searches_density = Double.parseDouble(args[6]);

        int num_searches = (int) (rows * columns * searches_density);

        // Initialize
        TerrainArea terrain = new TerrainArea(rows, columns, xmin, xmax, ymin, ymax);
        num_searches = (int) (rows * columns * searches_density);
        SearchParallel[] searches = new SearchParallel[num_searches];

        Random rand = new Random(); // the random number generator

        for (int i = 0; i < num_searches; i++) {
            searches[i] = new SearchParallel(i + 1, rand.nextInt(rows), rand.nextInt(columns), terrain);
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        MonteCarloMinimizationParallel task = new MonteCarloMinimizationParallel(0, num_searches, searches);

        tick();

        int result = forkJoinPool.invoke(task);

        tock();

        long time = endTime - startTime;

        System.out.printf("Run parameters\n");
        System.out.printf("\t Rows: %d, Columns: %d\n", rows, columns);
        System.out.printf("\t x: [%f, %f], y: [%f, %f]\n", xmin, xmax, ymin, ymax);
        System.out.printf("\t Search density: %f (%d searches)\n", searches_density, num_searches);

        /* Total computation time */
        System.out.printf("Time: %d ms\n", endTime - startTime);
        int tmp = terrain.getGrid_points_visited();
        System.out.printf("Grid points visited: %d  (%2.0f%s)\n", tmp, (tmp / (rows * columns * 1.0)) * 100.0, "%");
        tmp = terrain.getGrid_points_evaluated();
        System.out.printf("Grid points evaluated: %d  (%2.0f%s)\n", tmp, (tmp / (rows * columns * 1.0)) * 100.0, "%");

        /* Results */
        System.out.printf("Global minimum: %d at x=%.1f y=%.1f\n\n", min,
                terrain.getXcoord(searches[finder].getPos_row()), terrain.getYcoord(searches[finder].getPos_col()));

    }

}
