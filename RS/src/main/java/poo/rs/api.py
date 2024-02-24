from flask import Flask, jsonify, request
from flask_cors import CORS
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import linear_kernel

app = Flask(__name__)
CORS(app)

# Load data
data = pd.read_csv("Career.csv", encoding='UTF-8')

# Preprocessing data
data['Combined_skills'] = data['Hard Skills'] + ' ' + data['Soft skills']
data['Combined_skills'] = data['Combined_skills'].apply(lambda x: ' '.join(x.split(',')))

# Create a TF-IDF matrix for combined skills
tfidf_vectorizer = TfidfVectorizer(stop_words='english')
tfidf_matrix = tfidf_vectorizer.fit_transform(data['Combined_skills'])

@app.route('/recommend_jobs', methods=['POST'])
def recommend_jobs():
    try:
        # Get user input from JSON payload
        user_data = request.get_json()
        user_hard_skills = user_data['hard_skills']
        user_soft_skills = user_data['soft_skills']

        # Vectorize user skills
        user_skills = user_hard_skills + ' ' + user_soft_skills
        user_skills_vectorized = tfidf_vectorizer.transform([user_skills])

        # Calculate cosine similarities
        cosine_similarities = linear_kernel(user_skills_vectorized, tfidf_matrix).flatten()

        # Calculate similarity percentages
        similarity_percentages = cosine_similarities * 100

        # Sort jobs based on similarity
        related_jobs_indices = similarity_percentages.argsort()[::-1]
        top_10_jobs_indices = related_jobs_indices[:10]

        # Prepare results
        results = []
        for i, job_index in enumerate(top_10_jobs_indices):
            job_title = data['Job Title'].iloc[job_index]
            percentage = similarity_percentages[job_index]

            # Add Annual Salary to the results
            annual_salary = data['Annual salary'].iloc[job_index]

            results.append({"job_title": job_title, "percentage": percentage, "annual_salary": annual_salary})

        return jsonify({"results": results})

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)
