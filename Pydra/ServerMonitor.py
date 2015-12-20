__author__ = 'Hwaipy'
__version__ = 'v1.0.20151212'

from LabAtlas import Session, Message
import sys, time, os
from PyQt5.QtWidgets import QTableWidget, QMainWindow, QAbstractItemView, QTableWidgetItem, QDesktopWidget, \
    QApplication, QHeaderView, QHBoxLayout, QWidget, QTextEdit, QComboBox, QLineEdit, QCheckBox, QAction, QFileDialog
from PyQt5.QtGui import QIcon, QKeySequence
from PyQt5.QtCore import pyqtSignal
from threading import Thread


class MainFrame(QMainWindow):
    updateSummaryAction = pyqtSignal(object)

    def __init__(self):
        super().__init__()
        self.updateSummaryAction.connect(self.updateSummary)
        self.summary = None
        self.initUI()

    def initUI(self):
        # Setup table
        self.table = QTableWidget(0, 5)
        self.table.setEditTriggers(QAbstractItemView.NoEditTriggers)
        self.table.setSelectionBehavior(QAbstractItemView.SelectRows)
        self.table.setSelectionMode(QAbstractItemView.SingleSelection)
        self.table.setHorizontalHeaderLabels(['ID', 'Name', 'ConnectedTime', '--', '--'])
        self.table.horizontalHeader().setSectionResizeMode(QHeaderView.Stretch)
        self.table.currentCellChanged.connect(self.tableSelectionChanged)

        # setup textArea
        self.textArea = QTextEdit()
        self.textArea.setReadOnly(True)

        # Actions with Toolbar and Shortcut
        self.refreshAction = QAction(QIcon("resources/iris.png"), "Open", self)
        self.refreshAction.setShortcut(QKeySequence("Ctrl+O"))
        self.refreshAction.triggered.connect(self.actionOpen)
        self.toolbar = self.addToolBar('Toolbar')
        self.toolbar.setFloatable(False)
        self.toolbar.setMovable(False)
        self.toolbar.addAction(self.refreshAction)

        # Set main window
        self.mainWidget = QWidget(self)
        layout = QHBoxLayout()
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

    def updateSummary(self, summary):
        self.summary = summary
        self.table.setRowCount(len(summary))
        index = 0
        for summaryItem in summary:
            self.table.setItem(index, 0, QTableWidgetItem('{}'.format(summaryItem.__getitem__('ClientID'))))
            self.table.setItem(index, 1, QTableWidgetItem('{}'.format(summaryItem.__getitem__('Name'))))
            connectionTime = float(summaryItem.__getitem__('ConnectionTime'))
            self.table.setItem(index, 2, QTableWidgetItem('{}'.format((time.time() * 1000 - connectionTime))))
            index += 1

    def tableSelectionChanged(self, currentRow, currentColumn, previousRow, previousColumn):
        if self.summary != None:
            if currentRow >= 0:
                message = '{}'.format(self.summary[currentRow].__getitem__('ClientID'))
                self.textArea.setText(message)
                return
        self.textArea.clear()


def summaryReceiver(message):
    if message.content.__contains__('Summary'):
        summary = message.content.__getitem__('Summary')
        if ex != None:
            ex.updateSummaryAction.emit(summary)


ex = None

if __name__ == "__main__":
    print("This is monitor-hwaipy")
    client = Session('Monitor[Hwaipy]', ('localhost', 20102), [], {'SummaryRegistration': summaryReceiver})
    client.start(async=True)
    client.sendMessageLater(Message.createRequest('SummaryRegistration'))

    app = QApplication(sys.argv)
    ex = MainFrame()
    sys.exit(app.exec_())
