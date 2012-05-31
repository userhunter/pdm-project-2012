package it.pdm.project.MusicPlayer.objects;

public class MP3Item {
	private String m_strPath;
	private String m_strFileName;
	
	public MP3Item(String strPath, String strFileName) {
		this.setPath(strPath);
		this.setFileName(strFileName);
	}

	public String getPath() {
		return m_strPath;
	}

	public void setPath(String m_strPath) {
		this.m_strPath = m_strPath;
	}

	public String getFileName() {
		return m_strFileName;
	}

	public void setFileName(String m_strFileName) {
		this.m_strFileName = m_strFileName;
	}
}
