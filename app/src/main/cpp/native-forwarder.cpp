#include <jni.h>
#include <string>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <openssl/ssl.h>
#include <openssl/err.h>
#include <unistd.h>
#include <android/log.h>

#define BUFFER_SIZE 4096
#define RESPONSE_BUFFER_SIZE 1024

static const char* TELEGRAM_HOST = "api.telegram.org";
static const int TELEGRAM_PORT = 443;

std::string urlEncode(const std::string& str) {
    std::string encoded;
    for (char c : str) {
        if (isalnum(c) || c == '-' || c == '_' || c == '.' || c == '~') {
            encoded += c;
        } else if (c == ' ') {
            encoded += '+';
        } else {
            char hex[4];
            snprintf(hex, sizeof(hex), "%%%02X", (unsigned char)c);
            encoded += hex;
        }
    }
    return encoded;
}

bool sendTelegramMessage(const std::string& botToken,
                         const std::string& chatId,
                         const std::string& message) {
    SSL_CTX* ctx = nullptr;
    SSL* ssl = nullptr;
    int sockfd = -1;
    bool success = false;

    SSL_library_init();
    OpenSSL_add_all_algorithms();
    SSL_load_error_strings();

    ctx = SSL_CTX_new(TLS_client_method());
    if (!ctx) goto cleanup;

    SSL_CTX_set_min_proto_version(ctx, TLS1_3_VERSION);

    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) goto cleanup;

    struct timeval timeout;
    timeout.tv_sec = 10;
    timeout.tv_usec = 0;
    setsockopt(sockfd, SOL_SOCKET, SO_RCVTIMEO, &timeout, sizeof(timeout));
    setsockopt(sockfd, SOL_SOCKET, SO_SNDTIMEO, &timeout, sizeof(timeout));

    struct hostent* host = gethostbyname(TELEGRAM_HOST);
    if (!host) goto cleanup;

    struct sockaddr_in server_addr;
    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(TELEGRAM_PORT);
    memcpy(&server_addr.sin_addr.s_addr, host->h_addr, host->h_length);

    if (connect(sockfd, (struct sockaddr*)&server_addr, sizeof(server_addr)) < 0) goto cleanup;

    ssl = SSL_new(ctx);
    SSL_set_fd(ssl, sockfd);
    SSL_set_tlsext_host_name(ssl, TELEGRAM_HOST);

    if (SSL_connect(ssl) != 1) goto cleanup;

    std::string postData = "chat_id=" + urlEncode(chatId) +
                           "&text=" + urlEncode(message) +
                           "&parse_mode=HTML";

    std::string request = "POST /bot" + botToken + "/sendMessage HTTP/1.1\r\n";
    request += "Host: " + std::string(TELEGRAM_HOST) + "\r\n";
    request += "User-Agent: Mozilla/5.0\r\n";
    request += "Content-Type: application/x-www-form-urlencoded\r\n";
    request += "Content-Length: " + std::to_string(postData.length()) + "\r\n";
    request += "Connection: close\r\n\r\n";
    request += postData;

    if (SSL_write(ssl, request.c_str(), request.length()) <= 0) goto cleanup;

    char responseBuffer[RESPONSE_BUFFER_SIZE];
    memset(responseBuffer, 0, sizeof(responseBuffer));
    int read = SSL_read(ssl, responseBuffer, sizeof(responseBuffer) - 1);

    if (read > 0 && std::string(responseBuffer).find("200 OK") != std::string::npos) {
        success = true;
    }

cleanup:
    if (ssl) { SSL_shutdown(ssl); SSL_free(ssl); }
    if (sockfd >= 0) close(sockfd);
    if (ctx) SSL_CTX_free(ctx);
    return success;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_dev_test_myfirstapp_forwarders_TelegramForwarder_nativeTelegramSend(
    JNIEnv* env, jclass clazz, jstring botToken, jstring chatId, jstring message) {

    const char* botTokenStr = env->GetStringUTFChars(botToken, nullptr);
    const char* chatIdStr = env->GetStringUTFChars(chatId, nullptr);
    const char* messageStr = env->GetStringUTFChars(message, nullptr);

    std::string cppBotToken(botTokenStr);
    std::string cppChatId(chatIdStr);
    std::string cppMessage(messageStr);

    env->ReleaseStringUTFChars(botToken, botTokenStr);
    env->ReleaseStringUTFChars(chatId, chatIdStr);
    env->ReleaseStringUTFChars(message, messageStr);

    bool result = sendTelegramMessage(cppBotToken, cppChatId, cppMessage);
    return result ? JNI_TRUE : JNI_FALSE;
}