__author__ = 'Hwaipy'

import sys
from PyQt5.QtWidgets import QTableWidget, QAction, QMainWindow, QAbstractItemView, QTableWidgetItem, QDesktopWidget, \
    QApplication, QFileDialog
from PyQt5.QtGui import QIcon, QKeySequence
from PyQt5.QtCore import pyqtSignal
import re
import subprocess
from threading import Thread
import numpy as np


class Example(QMainWindow):
    reportAction = pyqtSignal(object)

    def __init__(self):
        super().__init__()

        # self.loadHosts()
        self.initUI()

    def initUI(self):
        # Refresh Action with Toolbar and Shortcut
        self.refreshAction = QAction(QIcon("resources/iris.png"), "Open", self)
        self.refreshAction.setShortcut(QKeySequence("Ctrl+O"))
        self.refreshAction.triggered.connect(self.actionOpen)
        self.toolbar = self.addToolBar('Toolbar')
        self.toolbar.addAction(self.refreshAction)

        # Set main window
        self.setGeometry(300, 300, 700, 500)
        self.setWindowTitle('LogViewer')
        self.centering()
        self.show()

    def centering(self):
        qr = self.frameGeometry()
        cp = QDesktopWidget().availableGeometry().center()
        qr.moveCenter(cp)
        self.move(qr.topLeft())

    def actionOpen(self):
        fname = QFileDialog.getOpenFileName(self, 'Open file', '.', '*.log')
        print(fname)


def show():
    app = QApplication(sys.argv)
    ex = Example()
    sys.exit(app.exec_())


if __name__ == '__main__':
    show()
