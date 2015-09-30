package arhivator;

import static java.lang.System.out;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

public class Script
{

	static volatile Process proc;
	static int in = 0;
	// static boolean sleep;
	// Form form;

	public Script()
	{
		// this.sleep = sleep;
	}

	private void print(String s) {
		Form.jt.setText(Form.jt.getText() + s + "\n");
		// Form.LOG(s);
		System.out.println(s);
	}

	public void DoScript() throws IOException, InterruptedException {

		//
		/*
		 * SwingUtilities.invokeLater(new Runnable() {
		 * 
		 * @Override public void run() { form = new Form(); } });
		 * 
		 * // TimeUnit.MINUTES.wait(2);//
		 */
		// System.exit(0);

		Logger log = Logger.getLogger("Arhivator");

		while (true) {

			in++;
			String d = new Date().toString();
			if (d.contains("Sun") || d.contains("Sat")) {// Sun
				print("Sun && Sat in Date. Pause 24h.");
				TimeUnit.HOURS.sleep(24);
			}
			Properties p = new Properties();

			File fileini = new File("arhivator.dat");
			if (fileini.exists()) {
				p.load(new FileReader(fileini));
				print("\n Настройки загружены из файла " + fileini.getCanonicalPath());
			} else {
				fileini.createNewFile();
				p.put("PathBackLoc", "D:\\backups\\data");
				p.put("PathBackRemout", "\\\\__\\");
				p.put("PathScripts", "D:\\backups\\scripts\\");
				p.store(new FileOutputStream(fileini), "ini for Arhivator " + new Date().toString());
				print("Создан файл настроек, его необходимо заполнить своими настройками! Файл находится:" + fileini.getCanonicalPath());
				System.exit(0);
			}
			// work with remote
			String PathBackRemout = p.getProperty("PathBackRemout");
			String PathBackLoc = p.getProperty("PathBackLoc");

			File fin = new File(PathBackRemout);
			long freeSpace = fin.getFreeSpace();
			long freeSp = freeSpace / 1024 / 1024 / 1024;
			print("Свободного места на диске- получателе: " + freeSp + " ГБ");
			String[] listIn = fin.list();
			List<ArrOfFile> arlist = new ArrayList<>();
			for (String str : listIn) {
				arlist.add(new ArrOfFile(PathBackRemout + str));
			}
			Collections.sort(arlist, (ArrOfFile o1, ArrOfFile o2) -> String.valueOf(o2.d).compareTo(String.valueOf(o1.d)));

			int deps = 0;
			String locstr = null;

			long deep = (freeSp - 5) / 1;

			if (deep < 0) {
				deep = 10;
				print("Места для хранения архивов нет, храним последние 10 архивов");
			}
			;

			print("Глубина хранения архивов - " + deep);
			for (ArrOfFile item : arlist) {
				deps++;
				if (deps <= deep) {
					continue;
				}
				locstr = "Удаляем " + item.toString();
				out.println(locstr);
				item.f.delete();
			} //
			if (locstr == null)
				out.println("Файлы на удаленном носителе удалены не были");

			print("Начинаем перемещение файлов.");
			File from = new File(PathBackLoc);
			String[] list = from.list();
			print("Обнаружено " + list.length + " файлов");
			if (list.length == 0) {
				print("Файлов нет, ничего не перемещаем.");
			}
			int iloc = 0;
			for (String f : list) {
				print(new Date() + " Перемещаем " + f);
				print("Объем перемещаемого файла: " + Files.size(new File(PathBackLoc + "\\" + f).toPath()) / 1024 / 1024 / 1024 + " ГБ");
				File fileloc = new File(PathBackLoc + "\\" + f);
				if (fileloc.isDirectory()) {
					print("Каталог, пропускаем");
					continue;
				}
				try {
					Files.move(fileloc.toPath(), new File(PathBackRemout + "\\" + f).toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (Exception e) {
					e.printStackTrace();
				}
				print(new Date() + " Перемещение " + f + " завершено");
				//
				print(f);
				iloc++;
			}
			print("Было перемещено " + iloc + " файлов"); // ***

			// Начинаем выгрузку данных из SQL
			File PathScripts = new File(p.getProperty("PathScripts"));
			String[] scripts = PathScripts.list();
			if (scripts == null) {
				print("Скриптов архивации не найдено!");
				System.exit(0);
			}
			print("Найдено скриптов архивации:" + scripts.length);

			for (String script : scripts) {
				print(new Date() + " выполняется скрипт " + script);
				new Thread(new Runnable() {

					@Override
					public void run() {
						ProcessBuilder pb = new ProcessBuilder(PathScripts + "\\" + script);
						pb.inheritIO();
						try {
							proc = pb.start();
						} catch (IOException ex) {
							Logger.getLogger(Arhivator.class.getName()).log(Level.SEVERE, null, ex);
							System.exit(1);
						}
					}
				}).start();

				TimeUnit.SECONDS.sleep(5);

				proc.waitFor();
				String fileresult;
				fileresult = p.getProperty("PathBackLoc") + "\\" + script.substring(0, script.length() - 4);
				// System.out.println(fileresult);
				print(new Date().toString() + " архивация `" + fileresult + "`. Выполнение завершено.");

				File bf = new File(fileresult);
				String newfstr = new Formatter().format("%tF %tR", new Date(), new Date()).toString().replace(":", "-");

				newfstr = fileresult + "_" + newfstr + ".backup";
				bf.renameTo(new File(newfstr));
				print("файл был переименован в " + newfstr);

				print(new Date() + " " + newfstr + " архивируется");
				FileInputStream tozip;
				try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(newfstr + ".zip"))) {
					zos.setComment(new Date().toString() + " class zipper.java");
					tozip = new FileInputStream(newfstr);
					byte[] buf = new byte[1024 * 100];
					int len;
					ZipEntry ze = new ZipEntry(newfstr);
					zos.putNextEntry(ze);
					while ((len = tozip.read(buf)) != -1) {
						zos.write(buf, 0, len);
					}
					zos.closeEntry();
					// zos.flush();
				}
				tozip.close();

				File backfile = new File(newfstr);
				backfile.delete();

				print(" создан " + backfile.getCanonicalPath() + " упакован в " + (backfile.getCanonicalPath() + ".zip"));
			}
			// log.info(in + new Date().toString() + "\n Пауза 24 ч.");
			int hour = Integer.valueOf(Form.hour.getText());
			print(in + " " + new Date().toString() + "\n Пауза " + hour + " ч.");
			// System.exit(0);

			if (Form.Jserv.isSelected())
				TimeUnit.HOURS.sleep(hour);
			else {
				JOptionPane.showMessageDialog(null, "Работа скрипта завершена!");
				return;
			}
			// System.exit(0);
			// переносим фокус управления в GUI

		}
	}

}
