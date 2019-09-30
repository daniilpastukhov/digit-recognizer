import smile.classification.LogisticRegression;
import smile.classification.SVM;
import smile.data.AttributeDataset;
import smile.math.kernel.GaussianKernel;
import tech.tablesaw.api.Table;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        Table train = Table.read().csv("mnist_png/mnist_train.csv");
        Table test = Table.read().csv("mnist_png/mnist_test.csv");

        AttributeDataset ds = train.smile().nominalDataset("label");
        AttributeDataset to_pred = test.smile().nominalDataset("label");

        SVM<double[]> clf = new SVM<>(new GaussianKernel(1), 1.0, 10, SVM.Multiclass.ONE_VS_ALL);
        clf.learn(ds.x(), ds.labels());

        for (int i = 0; i < ds.size(); i++) {
            double[] x = new double[784];
            for (int j = 0; j < ds.x().length; j++) {
                x[j] = ds.x()[i][j];
            }
            if (i % 500 == 0) System.out.print(i + " ");
            clf.learn(x, (int) ds.y()[i]);
        }

//        LogisticRegression clf = new LogisticRegression(ds.x(), ds.labels(), 0.00);
        int[] pred = clf.predict(to_pred.x());
        int correct = 0;
        int wrong = 0;
        List labels = train.column("label").asList();
        for (int i = 0; i < pred.length; i++) {
            if ((int) labels.get(i) == pred[i]) correct += 1;
            else wrong += 1;
        }

        System.out.println(correct + " " + wrong);


    }
}