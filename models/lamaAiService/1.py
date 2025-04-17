# server.py (Flask)
from flask import Flask, request, jsonify
from together import Together

app = Flask(__name__)
client = Together(api_key="c0cad587c1f7e331e13bc4f443b81630b6a2dc682ba69bc1adeb48e0f95168ce")

@app.route("/api/chat/", methods=["POST"])
def chat_with_llama():
    data = request.get_json()
    try:
        response = client.chat.completions.create(
            model="meta-llama/Llama-3-70b-chat-hf",
            messages=[{"role": "user", "content": data["message"]}],
            max_tokens=500,
        )
        return jsonify({
            "status": "success",
            "response": response.choices[0].message.content,
        })
    except Exception as e:
        return jsonify({"status": "error", "error": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5002)