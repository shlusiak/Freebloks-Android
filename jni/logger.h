/**
 * logger.h
 * Autor: Sascha Hlusiak
 *
 * Klasse zum Loggen von Ausgaben auf Konsole oder in Datei
 **/

#ifndef _LOGGER_H_INCLUDED_
#define _LOGGER_H_INCLUDED_

#include <stdarg.h>

class CLogWriter
{
private:
	CLogWriter* next;
public:
	CLogWriter() { next = NULL; }
	virtual ~CLogWriter();
	
	void addLogWriter(CLogWriter* _next);
	virtual void log(const char* fmt, va_list va);
};

class CStdoutWriter:public CLogWriter
{
public:
	virtual void log(const char* fmt, va_list va);
};

class CLogFileWriter:public CLogWriter
{
private:
	FILE *logfile;
public:
	CLogFileWriter();
	virtual ~CLogFileWriter();
	void closeFile();
	void flush();
	
	void createFile(const char* filename);
	virtual void log(const char* fmt, va_list va);
};


class CLogger
{
private:
	CLogWriter* writer;
protected:
	void logva(const char* fmt, va_list va);
public:
	CLogger(CLogWriter* _writer) { writer = _writer; }
	
	void log(const char* fmt, ...);
	virtual void logLine(const char* fmt, ...);
	void logTime(); 
	virtual void logHeader() { }
};

class CGameLogger:public CLogger
{
private:
	int game_number;
public:
	CGameLogger(CLogWriter* _writer, int _game_number);
	virtual void logHeader();
};




#endif
