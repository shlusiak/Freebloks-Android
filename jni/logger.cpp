/**
 * logger.h
 * Autor: Sascha Hlusiak
 *
 * Klasse zum Loggen von Ausgaben auf Konsole oder in Datei
 **/


#ifdef HAVE_CONFIG_H
  #include "config.h"
#endif

#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <stdio.h>
#include <stdarg.h>
#include "logger.h"


CLogWriter::~CLogWriter()
{
	if (next)
		delete next;
	next = NULL;
}

void CLogWriter::log(const char* fmt, va_list va) {
	if (next) {
		va_list vc;
		va_copy(vc, va);
		next->log(fmt, vc);
		va_end(vc);
	}
}

void CLogWriter::addLogWriter(CLogWriter* _next) {
	CLogWriter* cur = this;
	while (cur->next)
		cur = cur->next;
	cur->next = _next;
}



void CStdoutWriter::log(const char* fmt, va_list va) {
	CLogWriter::log(fmt, va);

	vprintf(fmt,va);
}




CLogFileWriter::CLogFileWriter()
{
	logfile = NULL;
}

CLogFileWriter::~CLogFileWriter()
{
	closeFile();
}

void CLogFileWriter::createFile(const char* filename)
{
	logfile=fopen(filename,"a");
	if (logfile)
	{

	} else {
		perror("fopen: ");
	}
}

void CLogFileWriter::closeFile()
{
	if (logfile)
	{
		flush();
		if (fclose(logfile))
			perror("fclose: ");
	}
	logfile=NULL;
}

void CLogFileWriter::flush()
{
	if (logfile)
		fflush(logfile);
}

void CLogFileWriter::log(const char* fmt, va_list va)
{
	CLogWriter::log(fmt, va);
	vfprintf(logfile, fmt,va);
	flush();
}



void CLogger::logva(const char* fmt, va_list va)
{
	if (writer)
		writer->log(fmt, va);
}

void CLogger::log(const char* fmt, ...)
{
	va_list va;
	va_start(va,fmt);
	logva(fmt, va);
	va_end(va);
}

void CLogger::logLine(const char* fmt, ...)
{
	va_list va;
	va_start(va,fmt);
	logHeader();
	logva(fmt, va);
	if (fmt[strlen(fmt)-1]!='\n')
		log("\n");
	va_end(va);
}


/**
 * Schreibt die aktuelle Uhrzeit
 **/
void CLogger::logTime()
{
	char zeitstring[256];
	time_t zeit;
	char *c;
	zeit=time(NULL);
	c=ctime(&zeit);
	strcpy(zeitstring,c);
	zeitstring[strlen(zeitstring)-1]='\0';
	log("         %s\n", zeitstring);
}




CGameLogger::CGameLogger(CLogWriter* _writer, int _game_number)
:CLogger(_writer)
{
	game_number=_game_number;
}

void CGameLogger::logHeader()
{
	if (game_number == 0) {
		log("[-]: ", game_number);
	} else {
		log("[%d]: ", game_number);
	}
}


