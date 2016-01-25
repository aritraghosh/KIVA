
public class ShowProgress {
	static int i;

	static void Show (){
		System.out.print('\b');

		char c='-';
		switch (i%6) {
		case 0: c='\\';
		break;
		case 1: c='|';
		break;
		case 2: c='/';
		break;
		case 3: c='-';
		break;
		case 4: c='|';
		break;
		case 5: c='-';
		break;
		}
		System.out.print(c);
		i++;
	}
}
