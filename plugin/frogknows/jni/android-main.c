#include <jni.h>
#include <android/log.h>
#include <pthread.h>
#include <string.h>

#include "constant.h"
#include "config.h"
#include "types.h"
#include "externs.h"
#include "curses.h"

static char android_files_path[1024];
static char android_savefile[50];
static int turn_save = 0;

char ANGBAND_TST       [1024]; //LIBDIR"/test"
char ANGBAND_HOU       [1024]; //LIBDIR"/files/hours"
char ANGBAND_MOR       [1024]; //LIBDIR"/files/news"
char ANGBAND_TOP       [1024]; //LIBDIR"/files/newscores"
char ANGBAND_BONES     [1024]; //LIBDIR"/bones/"
char ANGBAND_HELP      [1024]; //LIBDIR"/files/roglcmds.hlp"
char ANGBAND_ORIG_HELP [1024]; //LIBDIR"/files/origcmds.hlp"
char ANGBAND_WIZ_HELP  [1024]; //LIBDIR"/files/rwizcmds.hlp"
char ANGBAND_OWIZ_HELP [1024]; //LIBDIR"/files/owizcmds.hlp"
char ANGBAND_WELCOME   [1024]; //LIBDIR"/files/welcome.hlp"
char ANGBAND_LOG       [1024]; //LIBDIR"/files/ANGBAND.log"
char ANGBAND_VER       [1024]; //LIBDIR"/files/version.hlp"
char ANGBAND_LOAD      [1024]; //LIBDIR"/files/loadcheck"
char ANGBAND_WIZ       [1024]; //LIBDIR"/files/wizards"
char ANGBAND_SAV       [1024]; //LIBDIR"/save"

int8u wallsym = '#';
int8u floorsym = '.';

// Turbo C colors
int color_table[16] = {
	0xFF000000, //BLACK
	0xFF0040FF, //BLUE
	0xFF008040, //GREEN
	0xFF009090, //CYAN 0xFF00A0A0
	0xFFC00000, //RED
	0xFFFF00B0, //MAGENTA
	0xFFC08040, //BROWN
	0xFFC0C0C0, //LIGHTGRAY
	0xFF808080, //DARKGRAY
	0xFF00EFFF, //LIGHTBLUE
	0xFF00FF00, //LIGHTGREEN 
    0xFF10EFCC, //LIGHTCYAN 0xFF20FFDC
	0xFFFF5050, //LIGHTRED 0xFFFF4040
	0xFFA898EF, //LIGHTMAGENTA 0xFFB8A8FF
	0xFFFFFF00, //YELLOW
	0xFFFFFFFF  //WHITE
};

int file_exists(const char *fname)
{
	struct stat st;
	return (stat(fname, &st) == 0);
}

int angdroid_save() {
	int char_saved = FALSE;
	int err = FALSE;
	if (turn_save != turn 
		&& turn > 1
		&& !death) {

		char new_savefile[1024];
		char old_savefile[1024];

		/* New savefile */
		sprintf(new_savefile, "%s.new", savefile);
		sprintf(old_savefile, "%s.old", savefile);

		/* Make sure that the savefile doesn't already exist */
		unlink(new_savefile);
		unlink(old_savefile);

		gotoxy(1,MSG_LINE+1);
		clreol();
		put_buffer("Saving...",0,3);
		(void) strcpy (died_from, "(saved)");
		char_saved = _save_char(new_savefile,TRUE); //save and continue
		(void) strcpy (died_from, "(alive and well)");

		if (char_saved) {

			if (file_exists(savefile) && rename(savefile, old_savefile)!=0)
				err = TRUE;

			if (!err) {
				if (rename(new_savefile, savefile)!=0)
					err = TRUE;

				if (err)
					rename(old_savefile, savefile);
				else {
					unlink(old_savefile);
					turn_save = turn;

					put_buffer(" done.",0,12);
				}
			}

			if(err) {
				put_buffer(" failed.",0,12);
			}

			return err ? FALSE : TRUE;
		} 
		else {
			/* Delete temp file */
			unlink(new_savefile);
			put_buffer(" failed.",0,12);

			return FALSE;
		}
	}
	else {
		LOGD("angdroid_save.save skipped");
	}
}

void textbackground(int c){
}

void textcolor(int c){
	attrset(c);
}

/* Provides for a timeout on input. Does a non-blocking read, consuming the
   data if any, and then returns 1 if data was read, zero otherwise.

   Porting:

   In systems without the select call, but with a sleep for
   fractional numbers of seconds, one could sleep for the time
   and then check for input.

   In systems which can only sleep for whole number of seconds,
   you might sleep by writing a lot of nulls to the terminal, and
   waiting for them to drain, or you might hack a static
   accumulation of times to wait. When the accumulation reaches a
   certain point, sleep for a second. There would need to be a
   way of resetting the count, with a call made for commands like
   run or rest. */
int check_input(int microsec) {
	// sleep for microsec
	usleep(microsec);

	// if input available, consume & return 1
	int ch = angdroid_getch(0);

	return ch!=0;
}

/* Find a default user name from the system. */
void user_name(buf, id)
  char *buf;
  int id;
{
  (void) strcpy(buf, android_savefile);
}

int queryInt(const char* argv0) {
	int result = -1; //unknown command
	if (strcmp(argv0,"rl")==0) {
		result = 0;
		if (rogue_like_commands) result=1;
	}
	return result;
}

void angdroid_process_argv(int i, const char* argv) {
	switch(i){
	case 0: //files path
		my_strcpy(android_files_path, argv, strlen(argv)+1);
		break;
	case 1: //savefile
		my_strcpy(android_savefile, argv, strlen(argv)+1);
		break;
	default:
		break;
	}
}

void quit_hook() {
	if (c_list[MAX_CREATURES - 1].name[0] != ' ') 
		free(c_list[MAX_CREATURES - 1].name);
}

void initGame(void) {
	angdroid_quit_hook = quit_hook;

	strcpy(ANGBAND_TST,android_files_path);
	strcpy(ANGBAND_HOU,android_files_path);
	strcpy(ANGBAND_MOR,android_files_path);
	strcpy(ANGBAND_TOP,android_files_path);
	strcpy(ANGBAND_BONES,android_files_path);
	strcpy(ANGBAND_HELP,android_files_path);
	strcpy(ANGBAND_ORIG_HELP,android_files_path);
	strcpy(ANGBAND_WIZ_HELP,android_files_path);
	strcpy(ANGBAND_OWIZ_HELP,android_files_path);
	strcpy(ANGBAND_WELCOME,android_files_path);
	strcpy(ANGBAND_LOG,android_files_path);
	strcpy(ANGBAND_VER,android_files_path);
	strcpy(ANGBAND_LOAD,android_files_path);
	strcpy(ANGBAND_WIZ,android_files_path);
	strcpy(ANGBAND_SAV,android_files_path);

	strcat(ANGBAND_TST,"/test");
	strcat(ANGBAND_HOU,"/files/hours");
	strcat(ANGBAND_MOR,"/files/news");
	strcat(ANGBAND_TOP,"/save/newscores");
	strcat(ANGBAND_BONES,"/bones/");
	strcat(ANGBAND_HELP,"/files/roglcmds.hlp");
	strcat(ANGBAND_ORIG_HELP,"/files/origcmds.hlp");
	strcat(ANGBAND_WIZ_HELP,"/files/rwizcmds.hlp");
	strcat(ANGBAND_OWIZ_HELP,"/files/owizcmds.hlp");
	strcat(ANGBAND_WELCOME,"/files/welcome.hlp");
	strcat(ANGBAND_LOG,"/files/ANGBAND.log");
	strcat(ANGBAND_VER ,"/files/version.hlp");
	strcat(ANGBAND_LOAD,"/files/loadcheck");
	strcat(ANGBAND_WIZ ,"/files/wizards");
	strcat(ANGBAND_SAV ,"/save");

	// fix for needing re-writeable strings in creature list
	if (c_list[MAX_CREATURES - 1].name[0] == ' ') 
		c_list[MAX_CREATURES - 1].name = (char*)malloc(100);
	int i; for(i=0;i<99;i++) c_list[MAX_CREATURES - 1].name[i] = ' ';
	if (c_list[MAX_CREATURES - 1].name[0] == 'A') 
	c_list[MAX_CREATURES - 1].name[99]= '\0';

	initscr();
	for(i=0; i<16; i++)
		init_color(i, color_table[i]);
	for(i=0; i<16; i++)
		init_pair(i, i, 0);
}

void angdroid_main() {
	LOGD("angdroid_main()");
	initGame();

	//start frog
	char *a = "angband";
	char *args[2] = {a,NULL}; 

	attrset(COLOR_WHITE);
	curs_set(1);
	LOGD("main()");
	main(1,args);

	LOGD("angdroid_main exit normally");
	angdroid_quit(NULL);
}
