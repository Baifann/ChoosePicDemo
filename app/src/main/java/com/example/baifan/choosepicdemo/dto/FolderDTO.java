package com.example.baifan.choosepicdemo.dto;

/**
 * Created by baifan on 16/2/4.
 */
public class FolderDTO {
    /**
     * 当前文件夹路径
     */
    private String dir;
    /**
     * 当前文件夹第一张图片路径
     */
    private String firstImgPath;
    /**
     * 文件夹名称
     */
    private String name;
    /**
     * 当前文件夹数量
     */
    private int count;

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
        int lastIndexOf = this.dir.lastIndexOf("/");
        this.name = this.dir.substring(lastIndexOf);
    }

    public String getFirstImgPath() {
        return firstImgPath;
    }

    public void setFirstImgPath(String firstImgPath) {
        this.firstImgPath = firstImgPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FolderDTO folderDTO = (FolderDTO) o;

        return dir != null ? dir.equals(folderDTO.dir) : folderDTO.dir == null;

    }

    @Override
    public int hashCode() {
        return dir != null ? dir.hashCode() : 0;
    }
}
