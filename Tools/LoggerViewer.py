__author__ = 'Hwaipy'
__version__ = 'v0.20151009'

import sys
from PyQt5.QtWidgets import QTableWidget, QAction, QMainWindow, QAbstractItemView, QTableWidgetItem, QDesktopWidget, \
    QApplication, QFileDialog, QHeaderView, QVBoxLayout, QWidget
from PyQt5.QtGui import QIcon, QKeySequence
from PyQt5.QtCore import pyqtSignal
import configparser, os
import time, datetime
import re
from threading import Thread


class MainFrame(QMainWindow):
    logDataUpdateAction = pyqtSignal(object)

    def __init__(self, config):
        super().__init__()
        self.config = config
        self.logDataUpdateAction.connect(self.logDataUpdate)
        self.initUI()
        self.records = Records(self.logDataUpdateAction.emit)
        self.__loadNewFile = self.__loadLogFile(self.config.get('gui', 'fileopenpath', '.'))

    def initUI(self):
        # Actions with Toolbar and Shortcut
        self.refreshAction = QAction(QIcon("resources/iris.png"), "Open", self)
        self.refreshAction.setShortcut(QKeySequence("Ctrl+O"))
        self.refreshAction.triggered.connect(self.actionOpen)
        self.toolbar = self.addToolBar('Toolbar')
        self.toolbar.addAction(self.refreshAction)

        # Setup table
        self.table = QTableWidget(0, 5)
        self.table.setEditTriggers(QAbstractItemView.NoEditTriggers)
        self.table.setSelectionBehavior(QAbstractItemView.SelectRows)
        self.table.setSelectionMode(QAbstractItemView.SingleSelection)
        self.table.setHorizontalHeaderLabels(['Time', 'Level', 'Thread', 'Logger', 'Message'])
        self.table.horizontalHeader().setSectionResizeMode(QHeaderView.Stretch)

        # Set main window
        self.mainWidget = QWidget(self)
        layout = QVBoxLayout()
        self.mainWidget.setLayout(layout)
        layout.addWidget(self.table)
        layout.addWidget(QTableWidget(1, 1))
        self.setCentralWidget(self.mainWidget)
        self.setGeometry(0, 0, 1200, 600)
        self.setWindowTitle('LoggerViewer {}'.format(__version__))
        qr = self.frameGeometry()
        cp = QDesktopWidget().availableGeometry().center()
        qr.moveCenter(cp)
        self.move(qr.topLeft())
        self.show()

    def actionOpen(self):
        path = self.config.get('gui', 'fileopenpath', '.')
        fileName = QFileDialog.getOpenFileName(self, 'Open file', path, '*.log')[0]
        if (fileName.__len__() > 0):
            self.config.set('gui', 'fileopenpath', fileName)
            self.__loadNewFile(fileName)

    def logDataUpdate(self, event):
        action, records, index = event
        if index >= 0:
            self.table.setRowCount(records.__len__())
            record = records[index]
            self.table.setItem(index, 0, QTableWidgetItem(record.time.strftime('%Y-%m-%d %H:%M:%S.%f')))
            self.table.setItem(index, 1, QTableWidgetItem(record.level))
            self.table.setItem(index, 2, QTableWidgetItem(record.thread))
            self.table.setItem(index, 3, QTableWidgetItem(record.logger))
            self.table.setItem(index, 4, QTableWidgetItem(record.message))
        else:
            print('renew')

    def __loadLogFile(self, fileName):
        running = False
        logFileName = fileName

        def loadLoop():
            nonlocal running, logFileName
            file = None
            size = 0
            while running:
                try:
                    if file == None:
                        file = open(logFileName)
                        self.setWindowTitle('LoggerViewer {} [{}]'.format(__version__, logFileName))
                    newSize = os.path.getsize(logFileName)
                    if newSize == size:
                        time.sleep(1)
                        continue
                    if newSize > size:
                        newData = file.readlines()
                        size = newSize
                        self.records.appendData(newData)
                    if newSize < size:
                        file.tell(0)
                except:
                    print('e')
                    size = 0
                    if not file == None:
                        file.close()
                    self.setWindowTitle('LoggerViewer {}'.format(__version__))
                    file = None
                    time.sleep(1)

        def runLoop():
            nonlocal running, logFileName
            while True:
                running = True
                loadLoop()

        def controller(newFileName):
            nonlocal running, logFileName
            running = False
            logFileName = newFileName

        t = Thread(target=runLoop)
        t.setDaemon(True)
        t.start()
        return controller


class Records:
    def __init__(self, listener):
        self.logHeadPatternDate = re.compile('^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]$');
        self.logHeadPatternTime = re.compile('^[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\\.[0-9][0-9][0-9]$')
        self.logHeadPatternThread = re.compile('^\\[(.+)\\]$')
        self.listener = listener
        self.records = []
        if listener:
            listener(['renew', self.records, -1])

    def appendData(self, data):
        lines = [line[:-1] for line in data]
        for line in lines:
            record = self.__match(line)
            if record:
                self.__appendRecord(record)
            else:
                self.__updateRecord(line)

    def __appendRecord(self, record):
        self.records.append(record)
        if self.listener:
            self.listener(['add', self.records, self.records.__len__() - 1])

    def __updateRecord(self, line):
        record = self.records[-1]
        record.appendMessage(line)
        if self.listener:
            self.listener(['update', self.records, self.records.__len__() - 1])

    def __match(self, line):
        split = re.split(' +', line, 6)
        if split.__len__() == 7:
            if (not self.logHeadPatternDate.match(split[0]) == None) \
                    & (not self.logHeadPatternTime.match(split[1]) == None) \
                    & (not self.logHeadPatternThread.match(split[2]) == None):
                time = datetime.datetime.strptime(' '.join(split[:2]), '%Y-%m-%d %H:%M:%S.%f')
                return Record(time, split[2][1:-1], split[3], split[4], split[6])
        return None


class Record:
    def __init__(self, time, thread, level, logger, message):
        self.time = time
        self.thread = thread
        self.level = level
        self.logger = logger
        self.message = message

    def appendMessage(self, messageLine):
        self.message += '\n' + messageLine

    def __str__(self):
        return 'LogRecord {} [{}] {} {} - {}'.format(self.time, self.thread, self.level, self.logger, self.message)


class Config:
    def __init__(self, fileName):
        self.fileName = fileName
        self.configParser = configparser.ConfigParser()
        self.configParser.read(fileName)

    def get(self, section, option, default):
        modified = False
        if not self.configParser.has_section(section):
            self.configParser.add_section(section)
            modified = True
        if not self.configParser.has_option(section, option):
            self.configParser.set(section, option, default)
            modified = True
        if modified:
            f = open(self.fileName, 'w');
            self.configParser.write(f)
            f.close()
        return self.configParser.get(section, option)

    def set(self, section, option, value):
        if not self.configParser.has_section(section):
            self.configParser.add_section(section)
        self.configParser.set(section, option, value)
        f = open(self.fileName, 'w');
        self.configParser.write(f)
        f.close()


def show():
    app = QApplication(sys.argv)
    config = Config('LoggerViewer.conf')
    ex = MainFrame(config)
    sys.exit(app.exec_())


if __name__ == '__main__':
    show()
