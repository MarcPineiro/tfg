import os

def setSecureFilePermissions(dbPath):
    if os.name == 'posix':
        os.chmod(dbPath, 0o600)
    elif os.name == 'nt':
        import win32security, win32con
        sd = win32security.GetFileSecurity(dbPath, win32security.DACL_SECURITY_INFORMATION)
        dacl = win32security.ACL()
        user, domain, type = win32security.LookupAccountName("", os.getlogin())
        dacl.AddAccessAllowedAce(win32security.ACL_REVISION, win32con.FILE_GENERIC_READ | win32con.FILE_GENERIC_WRITE, user)
        sd.SetSecurityDescriptorDacl(1, dacl, 0)
        win32security.SetFileSecurity(dbPath, win32security.DACL_SECURITY_INFORMATION, sd)

def getSecureDbPath():
    hiddenDir = os.path.expanduser("~/.secure_app_data")
    if not os.path.exists(hiddenDir):
        os.makedirs(hiddenDir)
    return os.path.join(hiddenDir, "app_data.db")
