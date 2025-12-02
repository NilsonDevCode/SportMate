from firebase_admin import credentials, initialize_app, firestore
from tqdm import tqdm


class FirebaseUploader:
    def __init__(self, service_account_path):
        cred = credentials.Certificate(service_account_path)
        initialize_app(cred)
        self.db = firestore.client()

    def upload_hierarchy(self, communities: dict):
        for com_code, community in tqdm(communities.items(), desc='Communities'):
            com_ref = self.db.collection('communities').document(com_code)
            com_ref.set({
                "name": community['name'],
                "code": com_code
            })

            for prov_code, province in tqdm(community['provinces'].items(),
                                            desc=f'-> {community['name']}',
                                            leave=False):
                prov_ref = com_ref.collection('provinces').document(prov_code)
                prov_ref.set({
                    "name": province['name'],
                    "code": prov_code
                })

                batch = self.db.batch()
                count = 0

                for municipality in province["municipalities"]:
                    muni_ref = prov_ref.collection(
                        'municipalities').document(municipality['code'])
                    batch.set(muni_ref, {
                        "name": municipality['name'],
                        "code": municipality['code'],
                        "provinceCode": province['code'],
                        "provinceName": province['name'],
                        "communityCode": community['code'],
                        "communityName": community['name']
                    })

                    count += 1

                    if count % 400 == 0:
                        batch.commit()
                        batch = self.db.batch()

                if count % 400 != 0:
                    batch.commit()
