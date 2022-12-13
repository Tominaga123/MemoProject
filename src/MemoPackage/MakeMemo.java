package MemoPackage;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

class MemoFrame extends JFrame implements ActionListener, WindowListener,Runnable{

	JTextArea textArea = new JTextArea(30, 20); //メモを記入するところ
	JMenuBar menuBar = new JMenuBar(); //メニューバー
	JMenu memoList = new JMenu("メモ一覧"); //保存済みのメモのタイトルを一覧表示
	ArrayList<JMenuItem> memoItems = new ArrayList<>(); //保存済みのメモのタイトル
	JButton saveButton = new JButton("保存"); //保存ボタン
	JButton deleteButton = new JButton("削除"); //削除ボタン
	JButton newMemoButton = new JButton("新規作成"); //新規作成ボタン
	JButton referenceButton = new JButton("参照"); //他のファイルから参照する
	JButton titleButton = new JButton("タイトルを変える"); //タイトル変更を可能にするボタン
	JButton titleButton2 = new JButton("タイトル決定"); //タイトルを変更するボタン
	JTextField textField = new JTextField("", 20); //タイトルを表示するところ
	JPanel panel = new JPanel(); //タイトル関係のコンポーネントを乗せるパネル
	JFileChooser chooser = new JFileChooser(); //参照するために必要なもの
	File fileChoose; //参照した際に使う変数
	
	String path; //メモ保管庫へのパスを格納
	String newSentence = ""; //他のメモを開こうとする直前の文章を格納する変数
	String preSentence = ""; //メモを開いた直後の文章を格納する変数
	String newTitle = ""; //他のメモを開こうとする直前のタイトルを科右脳する変数
	String preTitle = ""; //メモを開いた直後のタイトルを格納する変数
	String[] fileList; //メモ一覧にタイトルを載せるために、メモ保管庫のメモファイル名を格納する変数
	static int flag = 0; //runメソッドで使用
	static int j = 0; //ConfirmFrame生成の際に数値を受け渡しするための変数
	int n = 1; //同一タイトルをナンバリングする際に使用
	
	File dir; //初めにメモ保管庫を作る時やメモ保管庫内のメモファイル一覧を取得するときに使用
	File file; //メモの入出力に使用
	File oldFile; //タイトルを変更する際に使用
	FileWriter fw; 
	FileReader fr;
	PrintWriter pw;
	BufferedWriter bw;
	BufferedReader br;
	
	MemoFrame(){
		setTitle("メモ");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		JScrollPane scrollPane = new JScrollPane(textArea);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		//メモファイルを保存するディレクトリを作成
		dir = new File("メモ保管庫"); //メモ保管庫の作成
		if(dir.mkdir()) {
			System.out.println("メモ保管庫を作成しました");
		} else {
			System.out.println("メモ保管庫の作成に失敗しましたか、既に作成されています");
		}
		path = dir.getAbsolutePath(); //メモ保管庫へのパスを取得
		System.out.println(path);
		//メモ一覧等をメニューバーに追加し、メニューバーをコンテナに追加
		fileList = dir.list();
		int i = 0;
		for(String s : fileList) {
			String sRemoved = s.replace(".txt", ""); //メモタイトルから「.txt」を削除
			memoItems.add(new JMenuItem(sRemoved)); //新しいメモタイトルを生成
			memoList.add(memoItems.get(i)); //メモ一覧にメモタイトルを追加
			i++;
		}
		menuBar.add(memoList);
		menuBar.add(saveButton);
		menuBar.add(deleteButton);
		menuBar.add(newMemoButton);
		menuBar.add(referenceButton);
		this.setJMenuBar(menuBar);
		//メモタイトルの入力フォームとメモタイトルの設定ボタンをパネルに配置しコンテナに追加
		panel.setLayout(new FlowLayout());
		panel.add(titleButton);
		panel.add(titleButton2);
		panel.add(textField);
		getContentPane().add(panel);
		//テキストを表示するエリアをコンテナに追加
		getContentPane().add(scrollPane);
		//イベント設定
		this.addWindowListener(this);
		for(i = 0; i < memoItems.size(); i++) { //メモ一覧のメモタイトルそれぞれにイベント設定
			memoItems.get(i).addActionListener(this);
		}
		memoList.addActionListener(this);
		saveButton.addActionListener(this);
		deleteButton.addActionListener(this);
		newMemoButton.addActionListener(this);
		titleButton.addActionListener(this);
		titleButton2.addActionListener(this);
		referenceButton.addActionListener(this);
		titleButton2.setEnabled(false); //タイトル変更はデフォルトではできないようにする
		textField.setEnabled(false);
		setSize(700,700);
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == saveButton) {
			//ディレクトリ「メモ保管庫」にファイルを作成する。メモ一覧にメモタイトルを追加する
			save();
		}else if(ae.getSource() == deleteButton) {
			//確認画面を出してからディレクトリからメモをを削除する。メモ一覧からも削除する
			newTitle = textField.getText();
			file = new File(path + "\\" + newTitle + ".txt");
			if(file.delete()) {
				System.out.println(newTitle + "を削除しました");
				textArea.setText(""); //白紙に戻す
				textField.setText("");
			}
			 preSentence = textArea.getText(); //変更の有無を確認するために文章を取得
			 preTitle = textField.getText(); //変更の有無を確認するためにタイトルを取得　必要??????????????????????????????????????????????
			arrange();
		}else if(ae.getSource() == newMemoButton) {
			//画面を白紙に戻す。タイトルは「新しいメモ」
			//直前に作業していたメモの変更が保存されていない場合は、保存するか確認画面を出す
			 newSentence = textArea.getText();
			 newTitle = textField.getText();
			 System.out.println(preSentence.equals(newSentence)); //文章の変更の有無を確認
			 if(preSentence.equals(newSentence) && preTitle.equals(newTitle)) { //タイトルの変更確認必要???????????????????????????????????/
				 textArea.setText(""); //白紙にする
				 textField.setText("新しいメモ"); //タイトルを「新しいメモ」にするタイトルが重複している場合、「新しいメモ(n)」とする
				 numbering(); //「新しいメモ」の
				 preSentence = textArea.getText();
				 preTitle = textField.getText();
				 System.out.println(preTitle + "が作成されました");
			 } else {
				 //変更が保存されていません。flag : 5保存、6保存しない、7キャンセル
				 flag = 4; 
				 ConfirmFrame cm = new ConfirmFrame(); //新しいフレームを生成して選択を待つ
				 Thread thread = new Thread(this);
				 thread.start();
			 }
		}else if(ae.getSource() == titleButton) {
			//タイトルを変更可能にする
			changeEnable(false); //いったんすべてのコンポーネントを使用不可する
			titleButton2.setEnabled(true); //タイトル変更のボタンのみ使用可能にする
			textField.setEnabled(true);
			preTitle = textField.getText(); //変更前のタイトルを取得
		}else if(ae.getSource() == titleButton2){
			//ディレクトリに保存されている当該メモファイルの名前を変更する
			//他のファイルと名前が重複している場合はそのメモを上書きするか確認 「名前(n+1)」の標記にするでいいのでは????????????????????
			//メモ一覧の名前も変える。
			newTitle = textField.getText(); //変更後のタイトルを取得
			if(preTitle.equals(newTitle)) { //変更前後のタイトルに変化がなければ何もせずコンポーネントを元に戻す
				changeEnable(true);
				titleButton2.setEnabled(false);
				textField.setEnabled(false);
				return;
			}
			oldFile = new File(path + "\\" + preTitle + ".txt"); //renameToメソッドを使用するために前のタイトルのファイルを生成
			file = new File(path + "\\" + newTitle + ".txt"); //renameToメソッドを使用するために新タイトルのファイルを生成
				if(file.exists()) {
					System.out.println("ファイル名 [" + newTitle + "] は既に存在しています。" + "\r\n" + "上書きしますか?");
					//同じ名前のファイルが既に存在しています。 9上書き、10キャンセル 「名前(n+1)」の標記にする選択肢を追加しては?????????????
					flag = 8;
					ConfirmFrame cm = new ConfirmFrame(newTitle); //新しいフレームを生成して選択を待つ
					Thread thread = new Thread(this);
					thread.start();
					return;
				}
			oldFile.renameTo(file); //タイトル変更
			System.out.println("タイトルを [" + newTitle + "] に変更しました");
			arrange(); //タイトル変更に伴いメモ一覧を整理
			changeEnable(true); //いったんすべてのコンポーネントを使用可能にする
			titleButton2.setEnabled(false); //タイトル変更のボタンのみ使用不可にする
			textField.setEnabled(false);
			//タイトルのみの変更なので、preSentence = textArea.getText();は必要なし
		}else if(ae.getSource() == referenceButton) { 
			//他のファイルから文章を参照する
			//直前に作業していたメモの変更が保存されていない場合は、保存するか確認画面を出す
			chooser.setSelectedFile(new File(path)); //デフォルトでMemoProjectのフォルダが開くようにする
			chooser.showOpenDialog(this); //新ウィンドウを開く
			fileChoose = chooser.getSelectedFile();
			newSentence = textArea.getText(); //変更の有無を確認するために文章を取得
			newTitle = textField.getText(); //変更の有無を確認するためにタイトルを取得　必要??????????????????????????????????????????????
			if(fileChoose != null) { //ファイルが選択されていれば文章を取得する
				 if(preSentence.equals(newSentence)) { //文章に変化がなければ何もしない
					 choose();
					 String str = newTitle.replace( "(" + n + ")", ""); //タイトルがナンバリングされている場合、ナンバリングを削除したものをコンソールで通知する
					 System.out.println("[" + str + "] を参照しました");
					 return;
					//preSentenceを更新すると、chooseメソッドによる文章の変更がなかったことになるので、preSentence = textArea.getText();は必要なし
				 } else {
					 //変更が保存されていません。flag : 12保存、13保存しない、14キャンセル
					 flag = 11; 
					 ConfirmFrame cm = new ConfirmFrame(); //新しいフレームを生成して選択を待つ
					 Thread thread = new Thread(this);
					 thread.start();
					 return;
				 }
			}else {
				System.out.println("参照は選択されませんでした"); //「取消」を押してもファイルが選択されていれば、選択されたことになる????????????
				//preSentenceを更新すると、保存してないのに文章が変更されていてもなかったことになるので、preSentence = textArea.getText();は必要なし
			}
		}else {  for(int i = 0; i < memoItems.size(); i++) { //メモ一覧内のどのメモタイトルが押されたか、メモ一覧を順に確認する
					 if(ae.getSource() == memoItems.get(i)) { 
						 //ディレクトリにあるメモファイルの名前を一覧表示する
						 //メモタイトルをクリックするとそれを画面に表示する
						 //直前に作業していたメモの変更が保存されていない場合は、保存するか確認画面を出す
							 System.out.println(memoItems.get(i).getText() + "がクリックされました");
							 newSentence = textArea.getText(); //変更の有無を確認するために文章を取得
							 newTitle = textField.getText(); //変更の有無を確認するためにタイトルを取得　必要??????????????????????????????????????????????
							 System.out.println(preSentence.equals(newSentence));
							 if(preSentence.equals(newSentence)) {
								 acquire(i); //作業中の文章に変更がなければ、ただファイルから文章を取得する
							 } else {
								 //変更が保存されていません。flag : 1保存、2保存しない、3キャンセル
								 flag = 0; 
								 j = i;
								 ConfirmFrame cm = new ConfirmFrame(); //新しいフレームを生成して選択を待つ
								 Thread thread = new Thread(this);
								 thread.start();
							 }

					 }
		         }
		}
	}
	
	public void save() { //文章を保存する
		try {
				newSentence = textArea.getText();
				newTitle = textField.getText();
				file = new File(path + "\\" + newTitle + ".txt"); 
				fw = new FileWriter(file);
				bw = new BufferedWriter(fw);
				pw = new PrintWriter(bw);
				pw.println(newSentence);
				pw.flush();
				System.out.println("タイトル" + newTitle + "で" + newSentence + "を書き込みました");
				preSentence = textArea.getText(); //変更の有無を確認するために文章を取得
//				preTitle = textField.getText(); //変更の有無を確認するためにタイトルを取得　必要??????????????????????????????
				arrange(); //メモ一覧を整理
				pw.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void arrange() { //いったんすべて削除したのちメモ一覧を再構成する
		memoList.removeAll(); 
		memoItems.removeAll(memoItems);
		fileList = dir.list();
		int i = 0;
		for(String s : fileList) {
			String sRemoved = s.replace(".txt", "");
			memoItems.add(new JMenuItem(sRemoved));
			memoList.add(memoItems.get(i));
			memoItems.get(i).addActionListener(this);
			i++;
		}
	}
	
	public void acquire(int i) { //メモ保管庫から文章を取得する
		 try {
			 textArea.setText("");
			 textField.setText(memoItems.get(i).getText());
			 file = new File(path + "\\" + memoItems.get(i).getText() + ".txt");
			 fr = new FileReader(file);
			 br = new BufferedReader(fr);
			 while((newSentence = br.readLine()) != null) {
				 textArea.append(newSentence + "\n");
				 textArea.setCaretPosition(textArea.getText().length());
			 }
			 preSentence = textArea.getText(); //変更の有無を確認するために文章を取得
			 preTitle = textField.getText(); //変更の有無を確認するためにタイトルを取得　必要??????????????????????????????????????
			 br.close();
		 }catch(IOException e) {
			 e.printStackTrace();
		 }
	}
	
	public void choose() { //参照から文章を取得する
		try {
			 textArea.setText("");
			 textField.setText("");
			 file = new File(fileChoose.getPath());
			 fr = new FileReader(file);
			 br = new BufferedReader(fr);
			 while((newSentence = br.readLine()) != null) {
				 textArea.append(newSentence + "\n");
				 textArea.setCaretPosition(textArea.getText().length());
			 }
			 String s = fileChoose.getName();
			 String sRemoved = s.replace(".txt", "");
			 textField.setText(sRemoved);
			 numbering(); //すでに同じメモタイトルが存在する場合、ナンバリングする
			 br.close();
		 }catch(IOException e) {
			 e.printStackTrace();
		 }
	}
	
	public void changeEnable(boolean b) { //コンポーネント使用の可能・不可能を切り替える
		memoList.setEnabled(b);
		saveButton.setEnabled(b);
		deleteButton.setEnabled(b);
		newMemoButton.setEnabled(b);
		titleButton.setEnabled(b);
		titleButton2.setEnabled(b);
		textField.setEnabled(b);
		textArea.setEnabled(b);
		referenceButton.setEnabled(b);
	}
	
	public void numbering() { //タイトルが重複している場合、「タイトル(n)」とする
		 n = 1;
		 newTitle = textField.getText();
	 	 file = new File(path + "\\" + newTitle + ".txt"); 
		 while(file.exists()) {
			 newTitle = newTitle.replace( "(" + n + ")", "");
			 n++;
			 newTitle = newTitle + "(" + n + ")";
			 file = new File(path + "\\" + newTitle + ".txt"); 
		 }
		 textField.setText(newTitle);
	}
	
	public void run() {
		System.out.println("スレッドスタート");
		changeEnable(false); //別フレームを開いている間、元フレームのコンポーネントを使えなくする
		while(flag == 0 || flag == 4 || flag == 8 || flag == 11) { //保存する、しない等が選択されるまで繰り返す
			try {
				System.out.println(flag);
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println(flag);
		 switch(flag) {
		 //メモ一覧から選んだ際の分岐(1～3)
		 case 1: //保存してから文章取得
			 save(); 
			 acquire(j);
			 break;
		 case 2: //保存せず文章取得
			 acquire(j);
		 case 3: //何もしない
			 break;
		 //新規作成の際の分岐(5～7)
		 case 5: //保存してから新規作成
			 save();
			 textArea.setText("");
			 textField.setText("新しいメモ");
			 numbering();
			 preSentence = textArea.getText(); //変更の有無を確認するために文章を取得
			 preTitle = textField.getText(); //変更の有無を確認するためにタイトルを取得　必要??????????????????
			 System.out.println(preTitle + "が作成されました");
			 break;
		 case 6: //保存せず新規作成
			 textArea.setText("");
			 textField.setText("新しいメモ");
			 numbering();
			 preSentence = textArea.getText(); //変更の有無を確認するために文章を取得
			 preTitle = textField.getText(); //変更の有無を確認するためにタイトルを取得　必要??????????????????
			 System.out.println(preTitle + "が作成されました");
		 case 7: //何もしない
			 break;
		 //タイトル変更の際の分岐(9～10)
		 case 9: //上書きする
			 save();
			 oldFile.delete();
			 preSentence = textArea.getText(); //変更の有無を確認するために文章を取得
			 preTitle = textField.getText(); //変更の有無を確認するためにタイトルを取得　必要??????????????????
			 arrange(); //タイトル変更に伴いメモ一覧を整理
			 System.out.println("[" + newTitle + "] を上書きしました");
		 case 10: //何もしない
			 break;
		 //参照から開く際の分岐(12～14)
		 case 12: //保存してから文章取得
			 save();
			 choose();
			 break;
		 case 13: //保存せず文章取得
			 choose();
		 case 14: //何もしない
			 break;
		 }
		changeEnable(true); //タイトル変更ボタンを除いて元フレームのコンポーネントを使えるようにする
		titleButton2.setEnabled(false);
		textField.setEnabled(false);
	}

	
	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		//作業しているメモの変更が保存されていない場合は、保存するか確認画面を出す。
//		flag = 0;
//		ConfirmFrame cm = new ConfirmFrame(newTitle);
//		Thread thread = new Thread(this);
//		thread.start();
////		try {
//			thread.join();
//		} catch (InterruptedException e1) {
//			// TODO 自動生成された catch ブロック
//			e1.printStackTrace();
//		}
		System.out.println("プログラムを終了します。");
    	System.exit(EXIT_ON_CLOSE);
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

}

class ConfirmFrame extends JFrame  implements ActionListener, WindowListener{
	JButton saveButton = new JButton("保存する");
	JButton noSaveButton = new JButton("保存しない");
	JButton cancelButton = new JButton("キャンセル");
	JButton overWriteButton = new JButton("上書きする");
	JButton cancelButton2 = new JButton("キャンセル");
	JLabel label = new JLabel("<html>編集中のメモがが保存されていません。<br>保存しますか？<html>");
	JLabel label2;
	JPanel panel = new JPanel();
	JPanel panel2 = new JPanel();
	JPanel panel3 = new JPanel();
	String newTitle;
	
	ConfirmFrame(){
		setTitle("確認");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		//ラベルを表示するエリアをコンテナに追加
		panel2.setLayout(new FlowLayout());
		panel2.add(label);
		getContentPane().add(panel2);
		//ボタンをパネルに配置しコンテナに追加
		panel.setLayout(new FlowLayout());
		panel.add(saveButton);
		panel.add(noSaveButton);
		panel.add(cancelButton);
		getContentPane().add(panel);
		//イベント設定
		this.addWindowListener(this);
		saveButton.addActionListener(this);
		noSaveButton.addActionListener(this);
		cancelButton.addActionListener(this);
		setSize(350,200);
		setVisible(true);
	}
	
	ConfirmFrame(String newTitle){
		this.newTitle = newTitle;
		setTitle("確認");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		//ラベルを表示するエリアをコンテナに追加
		panel3.setLayout(new FlowLayout());
		label2 = new JLabel("<html>ファイル名 [<html>" + newTitle + "<html>] は既に存在しています。<br>上書きしますか？<html>");
		panel3.add(label2);
		getContentPane().add(panel3);
		//ボタンをパネルに配置しコンテナに追加
		panel.setLayout(new FlowLayout());
		panel.add(overWriteButton);
		panel.add(cancelButton2);
		getContentPane().add(panel);
		//イベント設定
		this.addWindowListener(this);
		overWriteButton.addActionListener(this);
		cancelButton2.addActionListener(this);
		setSize(350,200);
		setVisible(true);
		
	}
	
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == saveButton) {
			//保存する
			MemoFrame.flag += 1;
		} else if(ae.getSource() == noSaveButton) {
			//保存しない
			MemoFrame.flag += 2;
		} else if(ae.getSource() == cancelButton) {
			//何も起こらない
			MemoFrame.flag += 3;
		} else if(ae.getSource() == overWriteButton) {
			MemoFrame.flag += 1;
		} else if(ae.getSource() == cancelButton2) {
			MemoFrame.flag += 2;
		}
		setVisible(false);
	}
	
	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		//キャンセルと同じ。何も起こらない
		MemoFrame.flag = 3;
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}
}

public class MakeMemo {
	public static void main(String[] args) {
		MemoFrame mf = new MemoFrame();
	}
}
