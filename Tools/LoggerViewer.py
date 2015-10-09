__author__ = 'Hwaipy'
__version__ = 'v1.20151009'

import sys
from PyQt5.QtWidgets import QTableWidget, QAction, QMainWindow, QAbstractItemView, QTableWidgetItem, QDesktopWidget, \
    QApplication, QFileDialog, QHeaderView, QVBoxLayout, QWidget, QTextEdit, QComboBox, QLineEdit, QCheckBox
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
        self.records = None
        self.searchText = ''
        self.re = False
        self.initUI()
        self.recordFilters = [self.__filterLevel, self.__filterSearch]
        self.records = Records(self.logDataUpdateAction.emit)
        self.__loadNewFile = self.__loadLogFile(self.config.get('gui', 'fileopenpath', '.'))

    def initUI(self):
        # Setup table
        self.table = QTableWidget(0, 5)
        self.table.setEditTriggers(QAbstractItemView.NoEditTriggers)
        self.table.setSelectionBehavior(QAbstractItemView.SelectRows)
        self.table.setSelectionMode(QAbstractItemView.SingleSelection)
        self.table.setHorizontalHeaderLabels(['Time', 'Level', 'Thread', 'Logger', 'Message'])
        self.table.horizontalHeader().setSectionResizeMode(QHeaderView.Stretch)
        self.table.currentCellChanged.connect(self.tableSelectionChanged)

        # setup textArea
        self.textArea = QTextEdit()
        self.textArea.setReadOnly(True)

        # Setup filtering Combo
        self.levelFilterCombo = QComboBox()
        self.levelFilterCombo.addItems(['ERROR', 'WARN', 'INFO', 'DEBUG', 'TRACE', 'ALL'])
        self.levelFilterCombo.currentIndexChanged.connect(self.levelComboSelectionChanged)
        self.levelFilterCombo.setCurrentText(self.config.get('gui', 'level', 'ALL'))

        # Setup search LineEdit
        self.searchBar = QLineEdit()
        self.searchBar.editingFinished.connect(self.searchTextChanged)

        # Setup RE RhackBox
        self.reCheckBox = QCheckBox()
        self.reCheckBox.setText('Regular Expression')
        self.reCheckBox.stateChanged.connect(self.searchTextChanged)

        # Actions with Toolbar and Shortcut
        self.refreshAction = QAction(QIcon("resources/iris.png"), "Open", self)
        self.refreshAction.setShortcut(QKeySequence("Ctrl+O"))
        self.refreshAction.triggered.connect(self.actionOpen)
        self.toolbar = self.addToolBar('Toolbar')
        self.toolbar.setFloatable(False)
        self.toolbar.setMovable(False)
        self.toolbar.addAction(self.refreshAction)
        self.toolbar.addWidget(self.levelFilterCombo)
        self.toolbar.addWidget(self.searchBar)
        self.toolbar.addWidget(self.reCheckBox)

        # Set main window
        self.mainWidget = QWidget(self)
        layout = QVBoxLayout()
        self.mainWidget.setLayout(layout)
        layout.addWidget(self.table)
        layout.addWidget(self.textArea)
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
        self.table.setRowCount(records.__len__())
        if index >= 0:
            record = records[index]
            self.table.setItem(index, 0, QTableWidgetItem(record.time.strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]))
            self.table.setItem(index, 1, QTableWidgetItem(record.level))
            self.table.setItem(index, 2, QTableWidgetItem(record.thread))
            self.table.setItem(index, 3, QTableWidgetItem(record.logger))
            message = '[*]' + record.message if record.message.__contains__('\n') else record.message
            self.table.setItem(index, 4, QTableWidgetItem(message))
            self.updateFiltering(index)
        else:
            self.textArea.clear()

    def tableSelectionChanged(self, currentRow, currentColumn, previousRow, previousColumn):
        if currentRow >= 0:
            message = self.records.records[currentRow].message
            self.textArea.setText(message)
        else:
            self.textArea.clear()

    def levelComboSelectionChanged(self, index):
        self.__showLevel = index
        if self.records:
            self.updateFiltering()

    def searchTextChanged(self):
        self.searchText = self.searchBar.text()
        self.re = self.reCheckBox.checkState()
        if self.records:
            self.updateFiltering()

    def updateFiltering(self, index=None):
        if not index == None:
            hide = False
            for filter in self.recordFilters:
                if not filter(self.records.records[index]):
                    hide = True
                    break
            if hide:
                self.table.hideRow(index)
            else:
                self.table.showRow(index)
        else:
            for i in range(self.records.records.__len__()):
                self.updateFiltering(i)


    def __filterLevel(self, record):
        return self.__showLevel >= record.getLevelIndex()

    def __filterSearch(self, record):
        return record.search(self.searchText, self.re)

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
                        self.records.clear()
                except:
                    print('e')
                    size = 0
                    if not file == None:
                        file.close()
                    self.records.clear()
                    self.setWindowTitle('LoggerViewer {}'.format(__version__))
                    file = None
                    time.sleep(1)
            self.records.clear()

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
        self.logHeadPatternDate = re.compile('^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]$')
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

    def clear(self):
        self.records.clear()
        if self.listener:
            self.listener(['renew', self.records, -1])


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
                return Record(time, split[2][1:-1], split[3], split[4], split[6], line)
        return None


class Record:
    LEVELS = ['ERROR', 'WARN', 'INFO', 'DEBUG', 'TRACE', 'ALL']

    def __init__(self, time, thread, level, logger, message, original):
        self.time = time
        self.thread = thread
        self.level = level
        self.logger = logger
        self.message = message
        if Record.LEVELS.__contains__(level):
            self.levelIndex = Record.LEVELS.index(level)
        else:
            self.levelIndex = Record.LEVELS.__len__() - 1
        self.original = original

    def appendMessage(self, messageLine):
        self.message += '\n' + messageLine

    def getLevelIndex(self):
        return self.levelIndex

    def search(self, searchText, useRe):
        if useRe:
            return re.findall(searchText, self.original).__len__() > 0
        else:
            for t in re.split(' +', searchText):
                if not self.original.lower().__contains__(t.lower()):
                    return False
            return True

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
