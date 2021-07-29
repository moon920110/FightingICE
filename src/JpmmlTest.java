import com.google.common.collect.Lists;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Model;
import org.jpmml.evaluator.*;
import org.jpmml.evaluator.visitors.DefaultVisitorBattery;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.*;

/**
 * FightingICEのメインメソッドを持つクラス．
 */
public class JpmmlTest {

	/**
	 * 起動時に入力した引数に応じて起動情報を設定し, それを基にゲームを開始する．<br>
	 * このメソッドはFightingICEのメインメソッドである．
	 *
	 * @param options
	 *            起動時に入力した全ての引数を格納した配列
	 */
	public static void main(String[] options) {
		try {
		    String row = "";
			List<Double> ret = new ArrayList<Double>();
			Map<FieldName, Object> arguments = new HashMap<FieldName, Object>();
			BufferedReader csvReader = new BufferedReader(new FileReader("D:\\DDA\\user_data\\total\\game-state.csv"));
			int idx = 0;
			int passIdx = 5182;
			int skipCnt = 0;

			while ((row = csvReader.readLine()) != null && idx < passIdx + 301) {
				if (idx > passIdx) {
				    if (skipCnt == 0) {
						List<String> tmpList = new ArrayList<String>();
						String[] data = row.split(",");
						tmpList = Arrays.asList(data);
						List<String> tmpSubList = Lists.newArrayList(tmpList.subList(12, tmpList.size()));
						int nameIdx = 65 * (idx - passIdx - 1) + 1;
						for (String s : tmpSubList) {
							ret.add(Double.parseDouble(s));
							String name = 'x' + Integer.toString(nameIdx);
//						System.out.println(s + " " + Double.parseDouble(s));
							arguments.put(FieldName.create(name), Double.parseDouble(s));
//						System.out.println(name + " " + arguments.get(FieldName.create(name)));
							nameIdx++;
						}
					}
					skipCnt++;
				    if (skipCnt < 15) {
				    	continue;
					} else {
				    	skipCnt = 0;
					}
				}
				idx++;
			}
//			System.out.println(ret.size());
//			System.out.println(ret);

			File anxietyModel = new File("./data/aiData/OurMcts/SVC5s15f_pipeline.pmml");
			Evaluator evaluator = new LoadingModelEvaluatorBuilder() {
			    @Override
				protected void checkSchema(ModelEvaluator<?> modelEvaluator) {
					Model model = modelEvaluator.getModel();
					MiningSchema miningSchema = model.getMiningSchema();
					List<InputField> inputFields = modelEvaluator.getInputFields();
					List<InputField> groupFields = Collections.emptyList();
					if (modelEvaluator instanceof HasGroupFields) {
						HasGroupFields hasGroupFields = (HasGroupFields)modelEvaluator;
						groupFields = hasGroupFields.getGroupFields();
					}

					if (inputFields.size() + groupFields.size() > 2000) {
						throw new InvalidElementException("Model has too many input fields", miningSchema);
					} else {
						List<TargetField> targetFields = modelEvaluator.getTargetFields();
						List<OutputField> outputFields = modelEvaluator.getOutputFields();
						if (targetFields.size() + outputFields.size() < 1) {
							throw new InvalidElementException("Model does not have any target or output fields", miningSchema);
						}
					}
				}
			}
					.setLocatable(false)
					.setVisitors(new DefaultVisitorBattery())
					.load(anxietyModel)
					.build();

			Map<FieldName, ?> results = evaluator.evaluate(arguments);
//			System.out.println(evaluator.getInputFields());
//			System.out.println(evaluator.getInputFields().size());
//			System.out.println(evaluator.getTargetFields());
//			System.out.println(evaluator.getOutputFields());
//			System.out.println(arguments);
			System.out.println(results);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} finally {

		}

//		Game game = new Game();
//		game.setOptions(options);
//		DisplayManager displayManager = new DisplayManager();
//
//		// ゲームの開始
//		displayManager.start(game);
	}
}
