(import 'com.sun.tools.attach.VirtualMachine)
(import 'java.lang.management.ManagementFactory)
(import '[java.io File PrintWriter ObjectOutputStream ObjectInputStream])
(import '[java.net Socket])
(import '[javax.tools ToolProvider DiagnosticCollector])
(import 'java.util.concurrent.Executors)
(import '[org.apache.commons.io IOUtils])
(require '[clojure.java.io :as io])

(load-file "commands.clj")

(defn jvm-pid
  "Get the PID of the current JVM. Not portable. Use with caution."
  []
  (-> (.. ManagementFactory getRuntimeMXBean getName)
      (clojure.string/split #"@")
      first))

(defn jar-path [jar-name]
  (.getPath (io/resource jar-name)))

(defonce classpath (System/getProperty "java.class.path"))
(defonce java-home (System/getProperty "java.home"))

(defonce bootjar-path (jar-path "btrace-boot.jar"))
(defonce agentjar-path (jar-path "btrace-agent.jar"))
(defonce clientjar-path (jar-path "btrace-client.jar"))
(defonce toolsjar-path (clojure.string/join java.io.File/separator
                          [(System/getProperty "java.home") ".." "lib" "tools.jar"]))


(defn- get-toolsjar-classpath []
  (first (filter #(.endsWith % "tools.jar")
                 (clojure.string/split classpath #":"))))

(defn- get-toolsjar-javahome []
  (let [file (File. (clojure.string/join java.io.File/separator
                      [(System/getProperty "java.home") ".." "lib" "tools.jar"]))]
    (if (.exists file)
      (.getPath file)
      nil)))

(defn- get-toolsjar-guess
  "Last resort"
  []
  (let [os-name (System/getProperty "os.name")]
    "/usr/java/latest/lib/tools.jar"))


(defn find-toolsjar []
  (let [toolsjar-classpath (get-toolsjar-classpath)
        toolsjar-javahome  (get-toolsjar-javahome)
        toolsjar-guess     (get-toolsjar-guess)]
    (some #(when-some [toolsjar %] toolsjar)
          [toolsjar-classpath toolsjar-javahome toolsjar-guess])))

;; ============================================================================
;; Agent
;; ============================================================================
;; for testing purpose
(defonce test-agent-args "port=3030,debug=true,unsafe=true,trackRetransforms=true,bootClassPath=/Users/myuen/.m2/repository/com/sun/tools/btrace/btrace-boot/1.2.5.1/btrace-boot-1.2.5.1.jar,systemClassPath=/Users/myuen/.jenv/versions/oracle64-1.8.0.25/lib/tools.jar,probeDescPath=.")

(defn attach-jvm [^String pid]
  (VirtualMachine/attach pid))

(defn attach-current-jvm []
  (attach-jvm (jvm-pid)))

(defn load-agent
  "Load btrace agent. TODO: Need to move agent args as parameters"
  [& {:keys [port debug unsafe systemClassPath bootClassPath trackRetransforms probeDescPath]
      :as arg-params}]
  (let [default-args {:port 3030 :debug true :unsafe true :systemClassPath (find-toolsjar)
                      :bootClassPath bootjar-path :trackRetransforms true :probeDescPath "."}
        merged-args (merge default-args arg-params)
        agent-args (clojure.string/join "," (for [[k v] merged-args] (clojure.string/join "=" [(name k) (str v)])))]
    (let [vm (attach-current-jvm)]
      (.loadAgent vm agentjar-path agent-args))))


;; ============================================================================
;; Compilation (Not easily doable). I will need to copy a lot of *.java files
;; from btrace. Might circle back.
;; ============================================================================
; (defn compile-btrace
;   []
;   (let [file (File. "Memory.java")
;         out (PrintWriter. System/out)
;         build-classpath (clojure.string/join File/pathSeparator [classpath clientjar-path])
;         compiler (ToolProvider/getSystemJavaCompiler)
;         std-manager (.getStandardFileManager compiler nil nil nil)
;         manager (MemoryJavaFileManager. std-manager nil)
;         comp-units (.getJavaFileObjects std-manager (into-array File [file]))
;         diagnostics (DiagnosticCollector.)
;         opts ["-Xlint:all" "-g:lines" "-deprecation" "-source" "1.6"
;               "-target" "1.6" "-sourcepath" "." "-classpath" build-classpath]
;         javac-task (.getTask compiler out manager diagnostics opts nil comp-units)]
;     ))

;; ============================================================================
;; Comm
;; ============================================================================
(defonce membytecode (IOUtils/toByteArray (io/input-stream "Memory.class")))

(defn submit []
  (let [_ (load-agent)
        bytecode (IOUtils/toByteArray (io/input-stream "Memory.class"))
        sock (Socket. "localhost" 3030)
        oos (ObjectOutputStream. (.getOutputStream sock))
        ois (ObjectInputStream. (.getInputStream sock))
        ic (instrument-command bytecode [])
        printThreadExec (Executors/newSingleThreadExecutor)]
    (writebytes ic oos)
    (loop []
      (let [cmd-type (.readByte ois)
            cmd (readbytes cmd-type ois)]
        (cond
          (contains? #{4 6 9} (:type cmd)) (printcmd cmd *out*)
          :else (println cmd)))
      (recur))
    ; (.start (Thread. #(loop []
    ;                     ; TODO polymorphic dispatch
    ;                     (let [cmd-type (.readByte ois)
    ;                           runtime (.readLong ois)
    ;                           len (.readInt ois)
    ;                           buffer (byte-array len)
    ;                           message (.read ois buffer 0 len)]
    ;                       (println (String. buffer "utf-8")))
    ;                     (recur))))
    ; (.writeByte oos (byte 3))
    ; (.writeInt oos (count bytecode))
    ; (.write oos bytecode)
    ; (.writeInt oos (byte 0))
    ; (.flush oos)
    sock))
