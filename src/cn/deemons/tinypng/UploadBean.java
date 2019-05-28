package cn.deemons.tinypng;

public class UploadBean {

    /**
     * input : {"size":861,"type":"image/png"}
     * output : {"size":822,"type":"image/png","width":60,"height":60,"ratio":0.9547,"url":"https://tinypng.com/web/output/t1ux4k2uvb043pg8880z5jjy6knvdqj7"}
     */

    private InputBean input;
    private OutputBean output;

    public InputBean getInput() {
        return input;
    }

    public void setInput(InputBean input) {
        this.input = input;
    }

    public OutputBean getOutput() {
        return output;
    }

    public void setOutput(OutputBean output) {
        this.output = output;
    }

    public static class InputBean {
        /**
         * size : 861
         * type : image/png
         */

        private int size;
        private String type;

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "InputBean{" +
                    "size=" + size +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    public static class OutputBean {
        /**
         * size : 822
         * type : image/png
         * width : 60
         * height : 60
         * ratio : 0.9547
         * url : https://tinypng.com/web/output/t1ux4k2uvb043pg8880z5jjy6knvdqj7
         */

        private int size;
        private String type;
        private int width;
        private int height;
        private double ratio;
        private String url;

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public double getRatio() {
            return ratio;
        }

        public void setRatio(double ratio) {
            this.ratio = ratio;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public String toString() {
            return "OutputBean{" +
                    "size=" + size +
                    ", type='" + type + '\'' +
                    ", width=" + width +
                    ", height=" + height +
                    ", ratio=" + ratio +
                    ", url='" + url + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "UploadBean{" +
                "input=" + input +
                ", output=" + output +
                '}';
    }
}
